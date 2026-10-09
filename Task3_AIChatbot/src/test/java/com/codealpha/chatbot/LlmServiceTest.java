package com.codealpha.chatbot;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.service.LlmService;
import com.codealpha.chatbot.service.SettingsService;
import com.codealpha.chatbot.storage.JsonStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

public class LlmServiceTest {

    private Path tempDir;
    private SettingsService settingsService;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("llm_test_");
        JsonStorageService storage = new JsonStorageService(tempDir);
        settingsService = new SettingsService(storage);
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.clearProperty("ai.api.key");
        System.clearProperty("openai.api.key");
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    public void testApiKeyResolutionFromSettingsOverride() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("sk-test-settings-key-12345");
        settingsService.updateSettings(settings);

        LlmService service = new LlmService(settingsService);
        assertTrue(service.isConfigured());
        assertEquals("sk-test-settings-key-12345", service.resolveApiKey());
        assertEquals("Settings (Manual Override)", service.getKeySource());
        assertTrue(service.getMaskedApiKey().contains("..."));
    }

    @Test
    public void testApiKeyResolutionFromSystemProperty() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("");
        settingsService.updateSettings(settings);

        System.setProperty("ai.api.key", "sk-test-sysprop-9999");
        LlmService service = new LlmService(settingsService);

        assertTrue(service.isConfigured());
        assertEquals("sk-test-sysprop-9999", service.resolveApiKey());
        assertEquals("System Property (ai.api.key)", service.getKeySource());
    }

    @Test
    public void testUnconfiguredServiceFailsGracefully() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("");
        settingsService.updateSettings(settings);

        LlmService service = new LlmService(settingsService);
        // Only assert if environment variables are not set
        if (!service.isConfigured()) {
            LlmService.LlmResponse response = service.generateResponse(
                    Collections.emptyList(), "What is quantum computing?", null);
            assertFalse(response.isSuccess());
            assertTrue(response.getErrorMessage().contains("AI API key is not configured"));
        }
    }

    @Test
    public void testSuccessfulResponseParsingWithMockClient() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("sk-mock-valid-key-1234");
        settingsService.updateSettings(settings);

        String mockResponseBody = """
                {
                  "id": "chatcmpl-mock",
                  "object": "chat.completion",
                  "model": "gpt-4o-mini",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "Python is a high-level programming language."
                      },
                      "finish_reason": "stop"
                    }
                  ]
                }
                """;

        HttpClient mockClient = createMockHttpClient(200, mockResponseBody);
        LlmService service = new LlmService(settingsService, mockClient);

        List<ChatMessage> history = List.of(
                ChatMessage.user("conv-1", "Tell me about programming languages")
        );

        LlmService.LlmResponse response = service.generateResponse(
                history, "What is Python?", null);

        assertTrue(response.isSuccess());
        assertEquals("Python is a high-level programming language.", response.getContent());
        assertEquals("gpt-4o-mini", response.getModelUsed());
    }

    @Test
    public void test401AuthenticationErrorHandling() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("sk-invalid-expired-key");
        settingsService.updateSettings(settings);

        HttpClient mockClient = createMockHttpClient(401, "{\"error\": {\"message\": \"Incorrect API key provided\"}}");
        LlmService service = new LlmService(settingsService, mockClient);

        LlmService.LlmResponse response = service.generateResponse(
                Collections.emptyList(), "Hello", null);

        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Authentication Error (401 Unauthorized)"));
    }

    @Test
    public void test429RateLimitErrorHandling() {
        UserSettings settings = settingsService.getSettings();
        settings.setLlmApiKeyOverride("sk-rate-limited-key");
        settingsService.updateSettings(settings);

        HttpClient mockClient = createMockHttpClient(429, "{\"error\": {\"message\": \"You exceeded your current quota\"}}");
        LlmService service = new LlmService(settingsService, mockClient);

        LlmService.LlmResponse response = service.generateResponse(
                Collections.emptyList(), "Test prompt", null);

        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Rate Limit / Quota Exceeded (429)"));
    }

    @SuppressWarnings("unchecked")
    private HttpClient createMockHttpClient(int statusCode, String responseBody) {
        return new HttpClient() {
            @Override
            public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
            @Override
            public Optional<Duration> connectTimeout() { return Optional.empty(); }
            @Override
            public Redirect followRedirects() { return Redirect.NEVER; }
            @Override
            public Optional<ProxySelector> proxy() { return Optional.empty(); }
            @Override
            public SSLContext sslContext() { return null; }
            @Override
            public SSLParameters sslParameters() { return null; }
            @Override
            public Optional<Authenticator> authenticator() { return Optional.empty(); }
            @Override
            public Version version() { return Version.HTTP_2; }
            @Override
            public Optional<Executor> executor() { return Optional.empty(); }

            @Override
            public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler) {
                return (HttpResponse<T>) new HttpResponse<String>() {
                    @Override public int statusCode() { return statusCode; }
                    @Override public HttpRequest request() { return request; }
                    @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
                    @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
                    @Override public String body() { return responseBody; }
                    @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
                    @Override public URI uri() { return request.uri(); }
                    @Override public Version version() { return Version.HTTP_2; }
                };
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler) {
                return CompletableFuture.completedFuture(send(request, responseHandler));
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
                return CompletableFuture.completedFuture(send(request, responseHandler));
            }
        };
    }
}
