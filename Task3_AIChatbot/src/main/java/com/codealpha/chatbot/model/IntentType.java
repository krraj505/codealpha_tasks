package com.codealpha.chatbot.model;

/**
 * Enumeration of all recognized intents supported by RAJ AI.
 */
public enum IntentType {
    GREETINGS("Greetings", "Conversational", "User greets the chatbot"),
    INTRODUCTION("Introduction", "Conversational", "User asks who the assistant is or what it can do"),
    GENERAL_HELP("General Help", "Support", "User asks for navigation or system help"),
    JAVA_PROGRAMMING("Java Programming", "Java Core", "General questions about Java and JVM"),
    OOP("Object-Oriented Programming", "Java Core", "Principles of Object-Oriented Programming"),
    VARIABLES_DATA_TYPES("Variables and Data Types", "Java Core", "Primitive types, variables, scope"),
    ARRAYS_ARRAYLISTS("Arrays and ArrayLists", "Java Core", "Fixed arrays, dynamic ArrayLists"),
    LOOPS_CONDITIONS("Loops and Conditions", "Control Flow", "if/else, switch, for, while loops"),
    CLASSES_OBJECTS("Classes and Objects", "OOP Concepts", "Class blueprints, object instantiation"),
    INHERITANCE("Inheritance", "OOP Concepts", "extends, super, code reuse"),
    POLYMORPHISM("Polymorphism", "OOP Concepts", "Overloading, overriding, dynamic dispatch"),
    ENCAPSULATION("Encapsulation", "OOP Concepts", "Getters, setters, access modifiers, data hiding"),
    ABSTRACTION("Abstraction", "OOP Concepts", "Abstract classes, interfaces"),
    EXCEPTION_HANDLING("Exception Handling", "Robustness", "try, catch, finally, custom exceptions"),
    COLLECTIONS("Collections", "Java Frameworks", "List, Set, Map, Queue, generics"),
    FILE_HANDLING("File Handling", "I/O Operations", "Files, Paths, BufferedReader, FileWriter"),
    STUDY_COLLEGE_FAQS("Study and College FAQs", "Academic", "CodeAlpha tasks, exams, syllabus prep"),
    TECHNOLOGY_FAQS("Technology FAQs", "Technology", "Software engineering, IDEs, git, career"),
    THANKS("Thanks", "Conversational", "User expresses gratitude"),
    GOODBYE("Goodbye", "Conversational", "User bids farewell"),
    UNKNOWN("Unknown or Unsupported Questions", "Fallback", "Low confidence or unmapped query");

    private final String displayName;
    private final String category;
    private final String description;

    IntentType(String displayName, String category, String description) {
        this.displayName = displayName;
        this.category = category;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public static IntentType fromDisplayName(String name) {
        if (name == null) return UNKNOWN;
        for (IntentType type : values()) {
            if (type.displayName.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
