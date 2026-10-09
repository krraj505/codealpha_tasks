# 🤖 RAJ AI — Desktop Java Programming Assistant

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![GUI](https://img.shields.io/badge/Swing-FlatLaf-3B82F6?style=for-the-badge)](https://www.formdev.com/flatlaf/)
[![Internship](https://img.shields.io/badge/CodeAlpha-Java%20Internship-8B5CF6?style=for-the-badge)](https://codealpha.tech/)
[![Build](https://img.shields.io/badge/Build-Passing-10B981?style=for-the-badge)]()

**RAJ AI** is a high-performance desktop AI Chatbot and intelligent Java pair programmer developed for **Task 3 (Artificial Intelligence Chatbot)** of the **CodeAlpha Java Programming Internship**. Built from scratch in **Java 17+** with a custom Swing UI/UX design system, rule-based NLP intent recognition engine, multi-turn conversation context tracking, live analytics dashboard, real-time knowledge base management, and thread-safe atomic local JSON persistence.

---

## 🌟 Executive Summary

Unlike standard introductory chatbots that rely on simple if-else statements or plain console I/O, **RAJ AI** is engineered as an enterprise-grade desktop software application. It delivers a modern AI user experience comparable to leading AI desktop tools, featuring:
- **Premium Design System**: Dark Navy (`#0A0F1D`) and Charcoal (`#131C33`) surfaces with Electric Blue (`#3B82F6`) and Violet (`#8B5CF6`) accents, plus dynamic 1-click Light Studio theme switching.
- **Minimalist RAJ AI Vector Logo**: Procedurally rendered vector badge in Graphics2D that scales crisp at all DPI levels.
- **Rule-Based NLP & Intent Recognition**: 21 recognized technical and conversational intents with tokenization, keyword extraction preserving programming terms, stemming, and fuzzy Levenshtein similarity.
- **Contextual Awareness & Follow-ups**: Maintains conversation state across turns. For example, asking *"Explain Java classes"* followed by *"Give me an example"* generates the class code snippet, and *"Explain it line by line"* breaks down the snippet sequentially.
- **Advanced Analytics Dashboard**: 4 custom-rendered vector charts (Area activity chart, Donut intent distribution, Bar category usage, Stacked quality meter) and KPI metrics calculated strictly from actual stored data.
- **Knowledge Base Catalog**: Dedicated management interface supporting search, category filtering, live Q&A editing, validation, duplicate prevention, and JSON import/export without requiring application rebuilds.
- **Thread-Safe Local Storage**: Atomic JSON writes with fallback templates preventing data corruption.

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
  - `Settings`: Themes, font sizing, NLP thresholds, and data management.
- **Search Conversations**: Real-time filtering of active and previous chat history.
- **Pinned & Recent Conversations**: Session history with right-click context menu (Rename, Pin/Unpin, Delete).
- **Theme Toggle & Collapse Control**: Instant switch between Dark Navy and Light Studio; sidebar collapses to compact icon strip.

### 2. Central Workspace
- **Chat View**:
  - *Empty State*: Welcoming assistant banner with 6 clickable prompt cards (OOP Pillars, Polymorphism, Classes & Objects, Exception Handling, Collections, Internship Advice).
  - *Message List*: User messages formatted in electric-blue rounded pills; Assistant responses formatted in charcoal cards with intent badges, response latency, and markdown-styled code blocks.
  - *Code Blocks*: Custom syntax boxes with language badge and 1-click **Copy Code** button.
  - *Typing Indicator*: Smooth, 3-dot animated pulse while the assistant processes queries in a background `SwingWorker` thread.
  - *Input Area*: Multiline input supporting `Enter` to send, `Shift + Enter` for newlines, and clear conversation triggers.
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
  - Dark Navy vs. Light Studio theme switcher.
  - Font scaling: Compact (12pt), Standard (14pt), Comfortable (16pt).
  - Confidence threshold slider (20% – 80%).
  - Auto-scroll and message timestamp toggles.
  - Safety-confirmed conversation clearing and KB reset.

### 3. Right Context Panel (Collapsible)
- Active conversation title and message counters.
- Real-time detected intent badge and confidence progress meter.
- Active knowledge base category and contextual topic summary.
- Fast 1-click shortcut buttons (New Chat, Clear Messages).
- Can be collapsed at any time to expand chat width.

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
│   │   │   │   ├── UserSettings.java          # Preference model (theme, font, thresholds)
│   │   │   │   └── AnalyticsSnapshot.java     # Immutable analytics model
│   │   │   ├── nlp/
│   │   │   │   ├── TextProcessor.java         # Normalization, tokenization, stemming
│   │   │   │   ├── RelevanceScorer.java       # Jaccard, token recall & Levenshtein scoring
│   │   │   │   ├── ContextManager.java        # Multi-turn conversational memory & follow-ups
│   │   │   │   └── IntentClassifier.java      # Rule-based regex & weighted intent classifier
│   │   │   ├── service/
│   │   │   │   ├── ChatbotEngine.java         # Core response coordinator & fallback recovery
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
│   │   │           ├── ContextPanel.java      # Collapsible right metadata & insights panel
│   │   │           ├── DashboardPanel.java    # Visual KPIs & analytics charts panel
│   │   │           ├── ModernChartPanel.java  # Vector Java2D charting engine (Area, Donut, Bar)
│   │   │           ├── KpiCard.java           # Metric card with badge & hover glow
│   │   │           ├── KnowledgeBasePanel.java# Q&A management table & modal dialogs
│   │   │           ├── SettingsPanel.java     # Preferences, thresholds, & about info
│   │   │           └── TypingIndicator.java   # Animated 3-dot pulse indicator
│   │   └── resources/
│   │       └── knowledge-base/
│   │           └── default_kb.json            # 18+ comprehensive seed Q&A entries
│   └── test/
│       └── java/com/codealpha/chatbot/
│           ├── TextProcessorTest.java         # Unit tests for text normalization & stemming
│           ├── RelevanceScorerTest.java       # Unit tests for Jaccard & Levenshtein distance
│           ├── IntentClassifierTest.java      # Unit tests for 21 intents & follow-ups
│           ├── ContextManagerTest.java        # Unit tests for state tracking & turn memory
│           ├── ChatbotEngineTest.java         # Integration tests for Q&A and fallbacks
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

## 🧠 NLP Implementation Details

The chatbot utilizes an explicit, transparent rule-based NLP pipeline:

1. **Text Normalization**:
   - Strips non-alphanumeric punctuation while safely preserving programming tokens (e.g. `do-while`, `C++`, `#`).
   - Normalizes redundant whitespace and lowercases input.
2. **Tokenization & Stop-Word Filtering**:
   - Filters general conversational stop-words (*is, are, the, at, by*).
   - **Crucially protects programming keywords**: tokens like `class`, `oop`, `java`, `variable`, `array`, `loop`, `inheritance`, `polymorphism`, `encapsulation`, `abstraction`, `exception`, `try`, `catch`, `finally`, `collection`, `file`, `git`, `maven` are never removed.
3. **Intent Classification (21 Supported Intents)**:
   - `Greetings`, `Introduction`, `General Help`
   - `Java Programming`, `Object-Oriented Programming`
   - `Variables and Data Types`, `Arrays and ArrayLists`
   - `Loops and Conditions`, `Classes and Objects`
   - `Inheritance`, `Polymorphism`, `Encapsulation`, `Abstraction`
   - `Exception Handling`, `Collections`, `File Handling`
   - `Study and College FAQs`, `Technology FAQs`
   - `Thanks`, `Goodbye`, `Unknown or Unsupported Questions`
4. **Relevance Scoring**:
   - Calculates a weighted score $S \in [0.0, 1.0]$:
     $$S = 0.55 \times \text{Recall} + 0.30 \times \text{Jaccard} + 0.15 \times \text{AnswerOverlap}$$
   - Includes Wagner-Fischer Levenshtein distance matching to accommodate minor user typos (e.g., *"polymorfism"* matches *"polymorphism"*).
5. **Multi-Turn Context Resolution**:
   - Tracks the active topic across turns in `ContextManager`.
   - When the user asks a follow-up:
     - *"Give me an example"* $\rightarrow$ retrieves and formats the code snippet of the active topic.
     - *"Explain it line by line"* $\rightarrow$ retrieves the sequential line-by-line breakdown of that snippet.
     - *"Tell me more"* $\rightarrow$ retrieves deep-dive architectural context.

---

## 💾 Local Storage & Reliability

- Data is persisted locally in the `data/` directory using JSON.
- **Atomic File Writing**: Writes changes to temporary files (`.tmp`) before performing an atomic filesystem move, preventing corruption during abrupt shutdowns.
- **First-Run Seeding**: Automatically loads `default_kb.json` containing 18+ high quality Java concepts on initial run.
- **Privacy & Safety**: Runtime data files in `data/` are strictly excluded via `.gitignore` to prevent committing user history or personal information.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (JDK) | 17+ (Tested on Java 25) | Core application logic |
| **GUI Framework** | Java Swing | Built-in | Desktop windowing and components |
| **Look & Feel** | FormDev FlatLaf | 3.4.1 | Modern dark/light styling & High-DPI |
| **JSON Parser** | Google Gson | 2.10.1 | Thread-safe data serialization |
| **Vector Graphics** | Java2D (Graphics2D) | Built-in | Procedural antialiased icons & charts |
| **Testing** | JUnit 5 Jupiter | 5.10.2 | Unit and integration testing |
| **Build Tool** | Apache Maven | 3.9+ | Dependency management & packaging |

---

## 🚀 Setup & Execution Guide

### Prerequisites
- **JDK 17 or higher** (Oracle JDK, OpenJDK, or Eclipse Temurin) installed.
- **Apache Maven 3.8+** installed (or use the preconfigured Maven installation).

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
Execute all 31 unit tests:
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
Double-click `run.bat` in the project directory, or run:
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

## 💬 Example Conversations

### Example 1: Multi-Turn Context & Follow-Up
```
User:
Explain Java classes and objects

Assistant [Intent: Classes and Objects • 95% confidence • 18ms]:
A Class is a user-defined blueprint or template from which individual objects
are created. An Object is an active instance of that class residing in heap memory.
...

User:
Give me an example

Assistant [Intent: Classes and Objects • 95% confidence • 12ms]:
Here is a code example for Explain Java classes and objects:
```java
public class Student {
    private String name;
    private int id;
    ...
}
```

User:
Explain it line by line

Assistant [Intent: Classes and Objects • 95% confidence • 14ms]:
### Line-by-Line Breakdown: Explain Java classes and objects
1. `public class Student`: Defines the class named Student.
2. `private String name, id`: Encapsulated instance state fields.
3. `public Student(String name, int id)`: Parameterized constructor initializes fields using `this`.
4. `Student s1 = new Student("Raj", 101)`: Allocates object in heap memory.
```

### Example 2: Polymorphism & Clarification
```
User:
polymorfism

Assistant [Intent: Polymorphism • 85% confidence • 22ms]:
Polymorphism means 'many forms'. It allows a single interface or method name
to execute different behaviors depending on how it is called or which object invokes it.
Two Types in Java:
1. Compile-Time Polymorphism (Method Overloading)
2. Runtime Polymorphism (Method Overriding)
```

---

## 🧪 Verification & Test Results

The project includes 9 dedicated test suites comprising 31 comprehensive unit tests:

| Test Class | Tests Run | Result | Coverage Area |
| :--- | :---: | :---: | :--- |
| `TextProcessorTest` | 4 | ✅ Pass | Normalization, tokenization, keyword preservation, follow-up detection |
| `RelevanceScorerTest` | 3 | ✅ Pass | Exact match scoring, Levenshtein fuzzy typos, negative filtering |
| `IntentClassifierTest` | 5 | ✅ Pass | 21 intents, greetings, KB matching, follow-ups, fallback recovery |
| `ContextManagerTest` | 3 | ✅ Pass | State initialization, topic tracking, reset behavior |
| `ChatbotEngineTest` | 3 | ✅ Pass | End-to-end processing, multi-turn follow-up chain, fallback messages |
| `KnowledgeBaseServiceTest` | 5 | ✅ Pass | CRUD operations, duplicate prevention, empty field validation, search |
| `ConversationServiceTest` | 4 | ✅ Pass | Session switching, message persistence, auto-title generation, pinning |
| `AnalyticsServiceTest` | 1 | ✅ Pass | KPI calculations, intent distribution tallies, average response times |
| `JsonStorageServiceTest` | 3 | ✅ Pass | Isolated JSON persistence for settings, sessions, and knowledge entries |
| **Total** | **31** | **✅ All 31 Passed (0 Failures, 0 Errors)** |

---

## 📌 Limitations & Future Enhancements

### Limitations
- The initial NLP engine is rule-based and knowledge-base backed, designed to provide accurate, deterministic answers to Java and OOP programming topics without hallucinating. It does not utilize an external cloud LLM API by default to maintain zero external network dependencies and offline capability.
- Multi-turn context memory is focused on recent conversational topics, follow-up examples, and line-by-line code breakdowns.

### Future Enhancements
- Integration of local ONNX models or optional OpenAI/Gemini API endpoints for open-ended conversational generation.
- Full syntax highlighting token styling for Java code blocks within Swing.
- Audio speech synthesis (Text-to-Speech) for voice-assisted explanations.

---

## 👤 Author & Submission Information

- **Internship**: CodeAlpha Java Programming Internship
- **Task**: Task 3 - Artificial Intelligence Chatbot
- **Developer**: Kumar Raj (`krraj505@gmail.com`)
- **Submission Repository**: `codealpha_tasks/Task3_AIChatbot`
