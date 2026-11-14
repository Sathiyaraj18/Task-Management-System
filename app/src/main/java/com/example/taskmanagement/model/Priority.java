package com.example.taskmanagement.model;

/**
 * Priority levels for tasks.
 */
public enum Priority {
    HIGH("High", 1),
    MEDIUM("Medium", 2),
    LOW("Low", 3);

    private final String displayName;
    private final int level;

    Priority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public static Priority fromString(String value) {
        if (value == null) return MEDIUM;
        for (Priority p : values()) {
            if (p.name().equalsIgnoreCase(value) || p.displayName.equalsIgnoreCase(value)) {
                return p;
            }
        }
        return MEDIUM;
    }
}
