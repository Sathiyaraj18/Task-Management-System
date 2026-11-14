package com.example.taskmanagement.model;

/**
 * Lifecycle status of a task.
 */
public enum TaskStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TaskStatus fromString(String value) {
        if (value == null) return PENDING;
        for (TaskStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) ||
                status.displayName.equalsIgnoreCase(value) ||
                status.name().replace("_", " ").equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
