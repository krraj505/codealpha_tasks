# 🤖 RAJ AI — General-Purpose Desktop AI Assistant & Pair Programmer

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![GUI](https://img.shields.io/badge/Swing-FlatLaf-3B82F6?style=for-the-badge)](https://www.formdev.com/flatlaf/)
[![Internship](https://img.shields.io/badge/CodeAlpha-Java%20Internship-8B5CF6?style=for-the-badge)](https://codealpha.tech/)
[![Build](https://img.shields.io/badge/Build-Passing-10B981?style=for-the-badge)]()
[![Tests](https://img.shields.io/badge/Tests-40%20Passed-10B981?style=for-the-badge)]()

**RAJ AI** is a high-performance desktop AI Assistant and intelligent pair programmer developed for **Task 3 (Artificial Intelligence Chatbot)** of the **CodeAlpha Java Programming Internship**. Built from scratch in **Java 17+** with a custom Swing UI/UX design system, general-purpose Large Language Model (LLM) integration, rule-based NLP intent recognition engine, multi-turn conversation context tracking, live analytics dashboard, real-time knowledge base management, and thread-safe atomic local JSON persistence.

---

## 🌟 Executive Summary

Unlike standard introductory chatbots that rely only on simple if-else statements or hardcoded responses, **RAJ AI** is an enterprise-grade desktop software application featuring a **Smart Hybrid Architecture**:
- **General-Purpose AI Text Assistant (ChatGPT Mode)**: Answers questions across all domains — Java, Python, C, C++, JavaScript, AI & Machine Learning, science, mathematics, history, writing, resume reviews, email drafts, debugging, and everyday reasoning.
- **Configurable LLM Integration**: Uses standard OpenAI-compatible REST endpoints supporting **OpenAI** (`gpt-4o-mini`, `gpt-4o`), **Groq Cloud** (`llama-3.3-70b-versatile`, `mixtral-8x7b`), **OpenRouter**, **DeepSeek**, and local **Ollama** (`llama3.2`).
- **Zero-Hardcoded Secrets**: API keys are securely resolved from environment variables (`AI_API_KEY`, `OPENAI_API_KEY`, `GROQ_API_KEY`, `GEMINI_API_KEY`), system properties, or optional settings overrides.
- **Asynchronous Non-Blocking HTTP**: Network calls run inside background `SwingWorker` threads using Java 11+ `java.net.http.HttpClient`, keeping the desktop GUI fluid and responsive at all times.
- **Multi-Turn Conversational Memory**: Passes historical conversation turns into the model so users can ask contextual follow-ups (*"Give me a Python example"*, *"Explain that line by line"*, *"How does this relate to the previous point?"*).
- **Fast Offline Knowledge Base**: Curated local Q&A catalog covering Core Java, Object-Oriented Principles, String Immutability, Collections, Exception Handling, File I/O, and CodeAlpha Internship FAQs with zero latency and 100% offline capability.
- **Premium Design System**: Dark Navy (`#0A0F1D`) and Charcoal (`#131C33`) surfaces with Electric Blue (`#3B82F6`) and Violet (`#8B5CF6`) accents, plus dynamic 1-click Light Studio theme switching.
- **Minimalist RAJ AI Vector Logo**: Procedurally rendered vector badge in Java2D Graphics2D that scales cleanly across High-DPI screens.
- **Advanced Analytics Dashboard**: 4 custom vector charts (Area activity chart, Donut intent distribution, Bar category usage, Stacked quality meter) and KPI telemetry calculated from stored records.
- **Thread-Safe Local Storage**: Atomic JSON writes with fallback templates preventing data corruption.

---

## ⚙️ AI Engine Configuration & Environment Variables

RAJ AI seamlessly operates in two modes:

### 1. Online General-Purpose AI Mode (Recommended)
Set your API key in your operating system environment before launching, or enter it in the **Settings** tab:

```bash
# Windows Command Prompt
setx AI_API_KEY "your_api_key_here"

# Windows PowerShell
$env:AI_API_KEY="your_api_key_here"

# macOS / Linux
export AI_API_KEY="your_api_key_here"
```

The application checks the following environment variables in order:
1. `AI_API_KEY`
2. `OPENAI_API_KEY`
3. `GROQ_API_KEY` (Fast inference via Groq Cloud)
4. `GEMINI_API_KEY`

### 2. In-App AI Settings
Navigate to the **Settings** tab in RAJ AI to customize:
- **AI Provider**: OpenAI, Groq Cloud, OpenRouter, Ollama (Local), or Custom Endpoint.
- **Model Name**: e.g., `gpt-4o-mini`, `llama-3.3-70b-versatile`, `deepseek-chat`.
- **API Base URL**: e.g., `https://api.openai.com/v1/chat/completions` or `https://api.groq.com/openai/v1/chat/completions`.
- **Test Connection**: 1-click connectivity verification displaying response latency and status.
- **Prefer Online AI**: Checkbox to route all queries directly to the online model.

### 3. Offline Knowledge Base Mode
If no API key is detected, RAJ AI automatically operates in **Offline Mode**, providing curated responses for Java, OOP, String Immutability, Collections, and CodeAlpha internship tasks, accompanied by clear instructions on enabling online AI.

---

## 📸 Application Screens & Navigation

The application window is organized into three balanced regions:

### 1. Left Sidebar
- **Branding & Logo**: Vector-rendered RAJ AI emblem with modern typography.
- **New Chat Action**: Primary button to initiate a new session instantly.
- **Navigation Menu**:
  - `Chat Assistant`: Primary interactive conversational workspace.
  - `Analytics`: Operational telemetry, charts, and metrics dashboard.
  - `Knowledge Base`: Q&A catalog editor and JSON data tools.
  - `Settings`: Themes, font sizing, AI model configuration, and storage maintenance.
- **Search Conversations**: Real-time filtering of active and previous chat history.
- **Pinned & Recent Conversations**: Session history with right-click context menu (Rename, Pin/Unpin, Delete).
- **Theme Toggle & Collapse Control**: Instant switch between Dark Navy and Light Studio; sidebar collapses to compact icon strip.

### 2. Central Workspace
- **Chat View**:
  - *Empty State*: Welcoming banner with 6 clickable prompt cards (OOP Pillars, String & Immutability, Polymorphism, Python vs Java, Exception Handling, CodeAlpha Advice).
  - *Message List*: User messages formatted in electric-blue rounded pills; Assistant responses formatted in charcoal cards with intent badges, response latency, and markdown-styled code blocks.
  - *Code Blocks*: Custom syntax boxes with language badge and 1-click **Copy Code** button.
  - *Typing Indicator*: Smooth, 3-dot animated pulse while the assistant processes queries in a background thread.
  - *Input Area*: Multiline input supporting `Enter` to send, `Shift + Enter` for newlines.
- **Analytics Dashboard**:
  - 4 KPI cards: Total Conversations, Total User Messages, Total Assistant Responses, Average Response Time (ms).
  - 4 vector charts: Activity Over Time, Intent Distribution, Category Usage, and Response Quality.
  - Recent activity log table with sorting and timestamps.
- **Knowledge Base Editor**:
  - Search bar and category dropdown filter.
  - Tabular list of entries with usage counters and timestamps.
  - Add / Edit / Delete modal dialogs with input validation.
  - JSON import and export file dialogs.
- **Settings & Personalization**:
  - AI Model and Provider configuration with online status badge and connection testing.
  - Dark Navy vs. Light Studio theme switcher.
  - Font scaling: Compact (12pt), Standard (14pt), Comfortable (16pt).
  - Local NLP confidence threshold slider (20% – 80%).
  - Auto-scroll and message timestamp toggles.
  - Safety-confirmed conversation clearing and KB reset.

### 3. Right Context Panel (Collapsible)
- Live AI Connection Status indicator (`● Online` vs. `○ Offline`).
- Active conversation title and message counters.
- Real-time detected intent badge and confidence progress meter.
- Active knowledge base category and contextual topic summary.
- Fast 1-click shortcut buttons (New Chat, Clear Messages).

---

## 🏗️ Architecture & Class Structure

The codebase adheres strictly to layered Object-Oriented architecture, separating data models, natural language processing, application services, storage, and custom UI components:

```
Task3_AIChatbot/
├── pom.xml                                    # Maven configuration & dependencies
├── README.md                                  # Complete project documentation
├── .gitignore                                 # Git ignore definitions
├── run.bat                                    # 1-Click Windows execution script
├── src/
│   ├── main/
│   │   ├── java/com/codealpha/chatbot/
│   │   │   ├── Main.java                      # Application entry point & High-DPI setup
│   │   │   ├── model/
│   │   │   │   ├── Role.java                  # Participant roles (USER, ASSISTANT, SYSTEM)
│   │   │   │   ├── IntentType.java            # 21 supported NLP intent classifications
│   │   │   │   ├── ChatMessage.java           # Message entity with timestamps & NLP metadata
│   │   │   │   ├── Conversation.java          # Session container with message lists & tags
│   │   │   │   ├── KnowledgeEntry.java        # Q&A data model with code snippets & follow-ups
│   │   │   │   ├── IntentMatchResult.java     # Classification output with confidence metrics
│   │   │   │   ├── UserSettings.java          # Preference model (theme, font, LLM settings)
│   │   │   │   └── AnalyticsSnapshot.java     # Immutable analytics model
│   │   │   ├── nlp/
│   │   │   │   ├── TextProcessor.java         # Normalization, tokenization, stemming
│   │   │   │   ├── RelevanceScorer.java       # Jaccard, token recall & Levenshtein scoring
│   │   │   │   ├── ContextManager.java        # Multi-turn conversational memory & follow-ups
│   │   │   │   └── IntentClassifier.java      # Rule-based regex & weighted intent classifier
│   │   │   ├── service/
│   │   │   │   ├── LlmService.java            # General-purpose OpenAI-compatible REST service
│   │   │   │   ├── ChatbotEngine.java         # Hybrid response coordinator & fallback recovery
│   │   │   │   ├── ConversationService.java   # Session lifecycle & auto-title generation
│   │   │   │   ├── KnowledgeBaseService.java  # Q&A catalog, search, and import/export
│   │   │   │   ├── AnalyticsService.java      # Telemetry calculations from stored data
│   │   │   │   └── SettingsService.java       # User preferences & reactive listeners
│   │   │   ├── storage/
│   │   │   │   ├── JsonStorageService.java    # Thread-safe atomic JSON file persistence
│   │   │   │   └── StorageException.java      # Storage error handler
│   │   │   └── ui/
│   │   │       ├── MainFrame.java             # Top-level window & view coordinator
│   │   │       ├── theme/
│   │   │       │   ├── ColorPalette.java      # Dark Navy & Light Studio color system
│   │   │       │   └── ThemeManager.java      # Dynamic Look & Feel and font manager
│   │   │       ├── util/
│   │   │       │   └── UiUtils.java           # Antialiasing & procedural vector icons
│   │   │       └── components/
│   │   │           ├── SidebarPanel.java      # Navigation, search, and history sidebar
│   │   │           ├── ChatPanel.java         # Chat workspace, empty state & input area
│   │   │           ├── MessageBubble.java     # Custom message card with code view & chips
│   │   │           ├── CodeBlockView.java     # Syntax code box with 1-click copy
│   │   │           ├── ContextPanel.java      # Collapsible right metadata & AI status panel
│   │   │           ├── DashboardPanel.java    # Visual KPIs & analytics charts panel
│   │   │           ├── ModernChartPanel.java  # Vector Java2D charting engine (Area, Donut, Bar)
│   │   │           ├── KpiCard.java           # Metric card with badge & hover glow
│   │   │           ├── KnowledgeBasePanel.java# Q&A management table & modal dialogs
│   │   │           ├── SettingsPanel.java     # LLM config, thresholds, & about info
│   │   │           └── TypingIndicator.java   # Animated 3-dot pulse indicator
│   │   └── resources/
│   │       └── knowledge-base/
│   │           └── default_kb.json            # 22+ comprehensive seed Q&A entries
│   └── test/
│       └── java/com/codealpha/chatbot/
│           ├── LlmServiceTest.java            # Unit tests for key resolution & mock API parsing
│           ├── TextProcessorTest.java         # Unit tests for text normalization & stemming
│           ├── RelevanceScorerTest.java       # Unit tests for Jaccard & Levenshtein distance
│           ├── IntentClassifierTest.java      # Unit tests for 21 intents & follow-ups
│           ├── ContextManagerTest.java        # Unit tests for state tracking & turn memory
│           ├── ChatbotEngineTest.java         # Tests for Q&A, WHAT IS STRING, and fallbacks
│           ├── KnowledgeBaseServiceTest.java  # Unit tests for CRUD, validation & duplicates
│           ├── ConversationServiceTest.java   # Unit tests for sessions & title generation
│           ├── AnalyticsServiceTest.java      # Unit tests for metric calculations
│           └── JsonStorageServiceTest.java    # Unit tests for thread-safe atomic persistence
└── data/                                      # Local runtime storage (git-ignored)
    ├── conversations.json
    ├── knowledge_base.json
    └── settings.json
```

---

## 🛠️ Technology Stack & Dependencies

| Component | Library / Framework | Version | Purpose |
| :--- | :--- | :---: | :--- |
| **Language** | Java SE (JDK) | 17+ | Core language platform |
| **GUI Framework** | Java Swing | Built-in | Desktop GUI component architecture |
| **Look & Feel** | FlatLaf (FormDev) | 3.4.1 | Clean modern desktop UI |
| **HTTP Client** | `java.net.http.HttpClient` | Built-in | Asynchronous REST communication |
| **JSON Parser** | Google Gson | 2.10.1 | Thread-safe data serialization |
| **Vector Graphics** | Java2D (Graphics2D) | Built-in | Procedural antialiased icons & charts |
| **Testing** | JUnit 5 Jupiter | 5.10.2 | Unit and integration testing |
| **Build Tool** | Apache Maven | 3.9+ | Dependency management & packaging |

---

## 🚀 Setup & Execution Guide

### Prerequisites
- **JDK 17 or higher** (Oracle JDK, OpenJDK, or Eclipse Temurin) installed.
- **Apache Maven 3.8+** installed.

Verify your environment:
```bash
java -version
mvn -version
```

### 1. Build the Project
Open a terminal in the `Task3_AIChatbot` directory:
```bash
cd Task3_AIChatbot
mvn clean compile
```

### 2. Run the Unit Test Suite
Execute all 40 unit tests:
```bash
mvn test
```

### 3. Package Executable JAR
Create both the standard jar and the standalone fat JAR:
```bash
mvn package -DskipTests
```
The executable fat JAR will be generated at:
`target/Task3_AIChatbot-1.0.0-jar-with-dependencies.jar`

### 4. Launch the Application
You can launch the chatbot using any of the following methods:

**Option A — 1-Click Batch Script (Windows):**
```cmd
run.bat
```

**Option B — Standalone Java Command:**
```cmd
java -jar target/Task3_AIChatbot-1.0.0-jar-with-dependencies.jar
```

**Option C — Maven Exec Plugin:**
```cmd
mvn exec:java -Dexec.mainClass="com.codealpha.chatbot.Main"
```

---

## 🧪 Verification & Test Results

The project includes 10 test suites comprising **40 comprehensive unit tests**:

| Test Class | Tests Run | Result | Coverage Area |
| :--- | :---: | :---: | :--- |
| `LlmServiceTest` | 6 | ✅ Pass | Key priority resolution, mock HTTP response, 401 & 429 error handling |
| `ChatbotEngineTest` | 6 | ✅ Pass | Knowledge lookup, WHAT IS STRING matching, multi-turn follow-ups, offline guidance |
| `TextProcessorTest` | 4 | ✅ Pass | Normalization, tokenization, keyword preservation, follow-up detection |
| `RelevanceScorerTest` | 3 | ✅ Pass | Exact match scoring, Levenshtein fuzzy typos, negative filtering |
| `IntentClassifierTest` | 5 | ✅ Pass | 21 intents, greetings, KB matching, follow-ups, fallback recovery |
| `ContextManagerTest` | 3 | ✅ Pass | State initialization, topic tracking, reset behavior |
| `KnowledgeBaseServiceTest` | 5 | ✅ Pass | CRUD operations, duplicate prevention, empty field validation, search |
| `ConversationServiceTest` | 4 | ✅ Pass | Session switching, message persistence, auto-title generation, pinning |
| `AnalyticsServiceTest` | 1 | ✅ Pass | KPI calculations, intent distribution tallies, average response times |
| `JsonStorageServiceTest` | 3 | ✅ Pass | Isolated JSON persistence for settings, sessions, and knowledge entries |
| **Total** | **40** | **✅ All 40 Passed (0 Failures, 0 Errors)** |

---

## 👤 Author & Submission Information

- **Internship**: CodeAlpha Java Programming Internship
- **Task**: Task 3 - Artificial Intelligence Chatbot
- **Application**: RAJ AI
- **Developer**: Kumar Raj (`krraj505@gmail.com`)
- **Submission Repository**: https://github.com/krraj505/codealpha_tasks
