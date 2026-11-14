package com.example.taskmanagement.model;

import com.example.taskmanagement.util.DateUtils;

import java.io.Serializable;

/**
 * Model representing a Task in the application.
 */
public class Task implements Serializable {
    private long id;
    private long userId;
    private String title;
    private String description;
    private String startDate;    // Format: yyyy-MM-dd
    private String dueDate;      // Format: yyyy-MM-dd
    private Priority priority;
    private TaskStatus status;
    private long createdAt;
    private long updatedAt;

    public Task() {
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Task(long id, long userId, String title, String description, String startDate,
                String dueDate, Priority priority, TaskStatus status, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : TaskStatus.PENDING;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Task(long userId, String title, String description, String startDate,
                String dueDate, Priority priority, TaskStatus status) {
        this(-1, userId, title, description, startDate, dueDate, priority, status,
                System.currentTimeMillis(), System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority != null ? priority : Priority.MEDIUM;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status != null ? status : TaskStatus.PENDING;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if the task is overdue (dueDate passed and not completed).
     */
    public boolean isOverdue() {
        if (status == TaskStatus.COMPLETED) {
            return false;
        }
        return DateUtils.isPastDate(dueDate);
    }

    /**
     * Helper to check if task is completed.
     */
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }
}
