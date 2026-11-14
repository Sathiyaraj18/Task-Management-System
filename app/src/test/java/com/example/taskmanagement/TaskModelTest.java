package com.example.taskmanagement;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.util.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Task model logic, Priority, Status, and ValidationUtils.
 */
public class TaskModelTest {

    @Test
    public void testTaskCreation() {
        Task task = new Task(1L, "Write Documentation", "Detailed docs", "2026-11-14", "2026-11-16",
                Priority.HIGH, TaskStatus.IN_PROGRESS);

        assertEquals("Write Documentation", task.getTitle());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertFalse(task.isCompleted());
    }

    @Test
    public void testPriorityFromString() {
        assertEquals(Priority.HIGH, Priority.fromString("HIGH"));
        assertEquals(Priority.HIGH, Priority.fromString("High"));
        assertEquals(Priority.MEDIUM, Priority.fromString("Medium"));
        assertEquals(Priority.LOW, Priority.fromString("low"));
        assertEquals(Priority.MEDIUM, Priority.fromString("unknown"));
    }

    @Test
    public void testTaskStatusFromString() {
        assertEquals(TaskStatus.PENDING, TaskStatus.fromString("Pending"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromString("In Progress"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromString("IN_PROGRESS"));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromString("Completed"));
        assertEquals(TaskStatus.PENDING, TaskStatus.fromString(null));
    }

    @Test
    public void testValidationUtils() {
        assertTrue(ValidationUtils.isValidTaskTitle("Buy Groceries"));
        assertFalse(ValidationUtils.isValidTaskTitle(""));
        assertFalse(ValidationUtils.isValidTaskTitle(" "));

        assertTrue(ValidationUtils.isValidUsername("developer"));
        assertFalse(ValidationUtils.isValidUsername("ab")); // Too short

        assertTrue(ValidationUtils.isValidPassword("secret123"));
        assertFalse(ValidationUtils.isValidPassword("12")); // Too short
    }
}
