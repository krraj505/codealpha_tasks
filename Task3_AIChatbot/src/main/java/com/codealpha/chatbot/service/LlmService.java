package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.model.UserSettings;
import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;

/**
 * Service for general-purpose AI / LLM integration via OpenAI-compatible endpoints.
 * Compatible with OpenAI (gpt-4o-mini, gpt-4o), Groq (llama-3.3-70b, mixtral),
 * OpenRouter, DeepSeek, LocalAI, and Ollama.
 *
 * Reads API keys from environment variables or settings overrides:
 * AI_API_KEY, OPENAI_API_KEY, GROQ_API_KEY, GEMINI_API_KEY.
 */
public class LlmService {

    public static final String DEFAULT_SYSTEM_PROMPT =
            "You are RAJ AI, a highly knowledgeable, helpful, and professional AI text assistant "
            + "developed by Kumar Raj for the CodeAlpha Java Programming Internship (Task 3).\n\n"
            + "You are a general-purpose AI assistant capable of answering questions across all domains including:\n"
            + "• Software engineering and programming (Java, Python, C, C++, JavaScript, Go, SQL, Web development, algorithms, debugging, code explanation)\n"
            + "• Artificial Intelligence, Machine Learning, Data Science, and Computer Architecture\n"
            + "• Mathematics, Science, History, Geography, and General Knowledge\n"
            + "• Technical writing, professional emails, resume reviews, applications, and interview preparation\n"
            + "• Everyday questions, logical reasoning, and multi-turn follow-up queries.\n\n"
            + "Always provide clear, direct, and well-structured responses. Use GitHub-flavored markdown formatting "
            + "with proper syntax-highlighted code blocks where helpful.";

    private final SettingsService settingsService;
    private final HttpClient httpClient;
    private final Gson gson;

    public LlmService(SettingsService settingsService) {
        this(settingsService, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build());
    }

    public LlmService(SettingsService settingsService, HttpClient httpClient) {
        this.settingsService = settingsService;
        this.httpClient = httpClient;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Resolves the active API key with hierarchical priority:
     * 1. Manual user override in application settings
     * 2. System property: ai.api.key or openai.api.key
     * 3. Environment variable: AI_API_KEY
     * 4. Environment variable: OPENAI_API_KEY
     * 5. Environment variable: GROQ_API_KEY
     * 6. Environment variable: GEMINI_API_KEY
     */
    public String resolveApiKey() {
        if (settingsService != null) {
            UserSettings settings = settingsService.getSettings();
            if (settings != null && settings.getLlmApiKeyOverride() != null && !settings.getLlmApiKeyOverride().isBlank()) {
                return settings.getLlmApiKeyOverride().trim();
            }
        }

        String sysProp = System.getProperty("ai.api.key");
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();

        sysProp = System.getProperty("openai.api.key");
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();

        String[] envVars = {"AI_API_KEY", "OPENAI_API_KEY", "GROQ_API_KEY", "GEMINI_API_KEY"};
        for (String var : envVars) {
            String val = System.getenv(var);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }

        return null;
    }

    /**
     * Checks if a valid API key is currently detected and available.
     */
    public boolean isConfigured() {
        String key = resolveApiKey();
        return key != null && !key.isBlank();
    }

    /**
     * Returns a user-friendly string indicating where the active key was discovered.
     */
    public String getKeySource() {
        if (settingsService != null) {
            UserSettings settings = settingsService.getSettings();
            if (settings != null && settings.getLlmApiKeyOverride() != null && !settings.getLlmApiKeyOverride().isBlank()) {
                return "Settings (Manual Override)";
            }
        }
        if (System.getProperty("ai.api.key") != null && !System.getProperty("ai.api.key").isBlank()) {
            return "System Property (ai.api.key)";
        }
        if (System.getProperty("openai.api.key") != null && !System.getProperty("openai.api.key").isBlank()) {
            return "System Property (openai.api.key)";
        }
        String[] envVars = {"AI_API_KEY", "OPENAI_API_KEY", "GROQ_API_KEY", "GEMINI_API_KEY"};
        for (String var : envVars) {
            String val = System.getenv(var);
            if (val != null && !val.isBlank()) {
                return "Environment (" + var + ")";
            }
        }
        return "Not Configured (Missing API Key)";
    }

    /**
     * Returns masked preview of the active API key for UI display (e.g. sk-a...4e1f).
     */
    public String getMaskedApiKey() {
        String key = resolveApiKey();
        if (key == null || key.isBlank()) {
            return "None (Offline Mode)";
        }
        if (key.length() <= 8) {
            return "********";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    public String getActiveBaseUrl() {
        if (settingsService != null) {
            return settingsService.getSettings().getLlmBaseUrl();
        }
        return "https://api.openai.com/v1/chat/completions";
    }

    public String getActiveModel() {
        if (settingsService != null) {
            return settingsService.getSettings().getLlmModel();
        }
        return "gpt-4o-mini";
    }

    /**
     * Generates a conversational AI response using multi-turn context.
     *
     * @param conversationHistory previous conversation messages for multi-turn awareness
     * @param currentQuery        the active user question
     * @param systemPrompt        optional custom system prompt (null for default)
     * @return LlmResponse with content and performance metadata
     */
    public LlmResponse generateResponse(List<ChatMessage> conversationHistory, String currentQuery, String systemPrompt) {
        long startTime = System.currentTimeMillis();

        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return LlmResponse.error(
                    "AI API key is not configured.\n\n"
                    + "To enable general-purpose AI responses (ChatGPT mode):\n"
                    + "1. Set the `AI_API_KEY` or `OPENAI_API_KEY` environment variable on your computer.\n"
                    + "2. Or configure your API key in the RAJ AI **Settings** tab.\n"
                    + "3. Any OpenAI-compatible provider is supported (OpenAI, Groq, OpenRouter, Ollama)."
            );
        }

        String baseUrl = getActiveBaseUrl();
        String model = getActiveModel();
        String effectiveSystemPrompt = (systemPrompt != null && !systemPrompt.isBlank())
                ? systemPrompt : DEFAULT_SYSTEM_PROMPT;

        try {
            // Build OpenAI-compatible request payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.addProperty("temperature", 0.7);
            payload.addProperty("max_tokens", 1600);

            JsonArray messagesArray = new JsonArray();

            // 1. System Prompt
            JsonObject sysMsg = new JsonObject();
            sysMsg.addProperty("role", "system");
            sysMsg.addProperty("content", effectiveSystemPrompt);
            messagesArray.add(sysMsg);

            // 2. Multi-turn conversation context (up to last 10 turns)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int startIdx = Math.max(0, conversationHistory.size() - 10);
                for (int i = startIdx; i < conversationHistory.size(); i++) {
                    ChatMessage msg = conversationHistory.get(i);
                    // Skip system or error messages
                    if (msg.getRole() == Role.SYSTEM || msg.getContent() == null || msg.getContent().isBlank()) {
                        continue;
                    }
                    // Avoid duplicating current query if already added to history
                    if (i == conversationHistory.size() - 1 && msg.getRole() == Role.USER && msg.getContent().equals(currentQuery)) {
                        continue;
                    }

                    JsonObject m = new JsonObject();
                    m.addProperty("role", msg.getRole() == Role.USER ? "user" : "assistant");
                    m.addProperty("content", msg.getContent());
                    messagesArray.add(m);
                }
            }

            // 3. Current User Query
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", currentQuery);
            messagesArray.add(userMsg);

            payload.add("messages", messagesArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("User-Agent", "RAJ-AI-Chatbot/1.0")
                    .timeout(Duration.ofSeconds(35))
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = Math.max(1, System.currentTimeMillis() - startTime);

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    String reply = messageObj.get("content").getAsString();
                    return LlmResponse.success(reply, latencyMs, model);
                } else {
                    return LlmResponse.error("API returned empty choices array.");
                }
            } else if (statusCode == 401) {
                return LlmResponse.error(
                        "Authentication Error (401 Unauthorized): Invalid or expired API key.\n\n"
                        + "Please verify your `AI_API_KEY` or `OPENAI_API_KEY` setting in environment variables or the Settings tab."
                );
            } else if (statusCode == 429) {
                return LlmResponse.error(
                        "Rate Limit / Quota Exceeded (429): Your API provider quota has been exhausted or rate limit was reached.\n\n"
                        + "Please check your account billing / rate limits on your provider dashboard."
                );
            } else if (statusCode == 404) {
                return LlmResponse.error(
                        "Endpoint or Model Not Found (404):\n"
                        + "Base URL: " + baseUrl + "\n"
                        + "Model: " + model + "\n\n"
                        + "Please check the model name and API endpoint in Settings."
                );
            } else {
                String errorSnippet = extractErrorMessage(responseBody);
                return LlmResponse.error("API Provider Error (HTTP " + statusCode + "): " + errorSnippet);
            }

        } catch (HttpTimeoutException e) {
            return LlmResponse.error("Request Timed Out: The AI provider did not respond within 35 seconds. Please try again.");
        } catch (IOException e) {
            return LlmResponse.error("Network Connection Failure: Unable to connect to " + baseUrl + " (" + e.getMessage() + "). Please check your internet connection.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return LlmResponse.error("Request was interrupted: " + e.getMessage());
        } catch (Exception e) {
            return LlmResponse.error("Unexpected error during AI generation: " + e.getMessage());
        }
    }

    /**
     * Tests connectivity to the configured AI endpoint.
     */
    public LlmResponse testConnection() {
        return generateResponse(Collections.emptyList(), "Respond with exactly: 'Connected to RAJ AI successfully.'", "You are an automated connectivity verification test.");
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) return "Unknown error response from server";
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("error")) {
                JsonElement errEl = json.get("error");
                if (errEl.isJsonObject()) {
                    JsonObject errObj = errEl.getAsJsonObject();
                    if (errObj.has("message")) {
                        return errObj.get("message").getAsString();
                    }
                } else if (errEl.isJsonPrimitive()) {
                    return errEl.getAsString();
                }
            }
        } catch (Exception ignored) {}
        return (body.length() > 200) ? body.substring(0, 200) + "..." : body;
    }

    /**
     * DTO containing response result or error details.
     */
    public static class LlmResponse {
        private final boolean success;
        private final String content;
        private final String errorMessage;
        private final long latencyMs;
        private final String modelUsed;

        private LlmResponse(boolean success, String content, String errorMessage, long latencyMs, String modelUsed) {
            this.success = success;
            this.content = content;
            this.errorMessage = errorMessage;
            this.latencyMs = latencyMs;
            this.modelUsed = modelUsed;
        }

        public static LlmResponse success(String content, long latencyMs, String modelUsed) {
            return new LlmResponse(true, content, null, latencyMs, modelUsed);
        }

        public static LlmResponse error(String errorMessage) {
            return new LlmResponse(false, null, errorMessage, 0, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getContent() {
            return content;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public String getModelUsed() {
            return modelUsed != null ? modelUsed : "General AI";
        }
    }
}
