package com.example.taskmanagement.repository;

import android.content.Context;

import com.example.taskmanagement.data.DatabaseHelper;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.util.AppExecutors;

import java.util.List;

/**
 * Repository handling Task data operations with asynchronous execution off the main UI thread.
 */
public class TaskRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }

    private final DatabaseHelper dbHelper;
    private final AppExecutors executors;

    public TaskRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executors = AppExecutors.getInstance();
    }

    public void insertTask(Task task, Callback<Long> callback) {
        executors.diskIO().execute(() -> {
            try {
                long id = dbHelper.insertTask(task);
                executors.mainThread().execute(() -> {
                    if (callback != null) {
                        if (id > 0) {
                            task.setId(id);
                            callback.onSuccess(id);
                        } else {
                            callback.onError(new Exception("Failed to insert task"));
                        }
                    }
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void updateTask(Task task, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                int rows = dbHelper.updateTask(task);
                executors.mainThread().execute(() -> {
                    if (callback != null) {
                        callback.onSuccess(rows > 0);
                    }
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void updateTaskStatus(long taskId, TaskStatus status, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                int rows = dbHelper.updateTaskStatus(taskId, status);
                executors.mainThread().execute(() -> {
                    if (callback != null) {
                        callback.onSuccess(rows > 0);
                    }
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void deleteTask(long taskId, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                int rows = dbHelper.deleteTask(taskId);
                executors.mainThread().execute(() -> {
                    if (callback != null) {
                        callback.onSuccess(rows > 0);
                    }
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void getTaskById(long taskId, Callback<Task> callback) {
        executors.diskIO().execute(() -> {
            try {
                Task task = dbHelper.getTaskById(taskId);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(task);
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void getFilteredTasks(long userId, String query, String statusFilter,
                                 String priorityFilter, String sortBy, Callback<List<Task>> callback) {
        executors.diskIO().execute(() -> {
            try {
                List<Task> tasks = dbHelper.getFilteredTasks(userId, query, statusFilter, priorityFilter, sortBy);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(tasks);
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void getDashboardStats(long userId, Callback<DatabaseHelper.DashboardStats> callback) {
        executors.diskIO().execute(() -> {
            try {
                DatabaseHelper.DashboardStats stats = dbHelper.getDashboardStats(userId);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(stats);
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void getRecentTasks(long userId, int limit, Callback<List<Task>> callback) {
        executors.diskIO().execute(() -> {
            try {
                List<Task> tasks = dbHelper.getRecentTasks(userId, limit);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(tasks);
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }
}
