package com.example.taskmanagement.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagement.R;
import com.example.taskmanagement.adapter.TaskAdapter;
import com.example.taskmanagement.data.DatabaseHelper;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.ui.auth.LoginActivity;
import com.example.taskmanagement.ui.task.AddEditTaskActivity;
import com.example.taskmanagement.ui.task.TaskDetailActivity;
import com.example.taskmanagement.ui.task.TaskListActivity;
import com.example.taskmanagement.util.Constants;
import com.example.taskmanagement.util.DateUtils;
import com.example.taskmanagement.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Date;
import java.util.List;

/**
 * Main dashboard screen displaying real-time task KPI metrics (Total, Pending,
 * In Progress, Completed), quick navigation, recent task preview, and logout action.
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvCurrentDate;
    private ImageButton btnLogout;

    // Stat KPI Cards & Text
    private MaterialCardView cardTotalTasks;
    private MaterialCardView cardPendingTasks;
    private MaterialCardView cardInProgressTasks;
    private MaterialCardView cardCompletedTasks;

    private TextView tvStatTotalCount;
    private TextView tvStatPendingCount;
    private TextView tvStatInProgressCount;
    private TextView tvStatCompletedCount;

    // Actions & Recent Section
    private MaterialButton btnViewAllTasks;
    private MaterialButton btnQuickAddTask;
    private TextView tvSeeAllRecent;
    private TextView tvEmptyRecent;
    private RecyclerView rvRecentTasks;
    private ExtendedFloatingActionButton fabAddTask;

    private TaskAdapter recentTaskAdapter;
    private TaskRepository taskRepository;
    private SessionManager sessionManager;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        currentUserId = sessionManager.getUserId();
        taskRepository = new TaskRepository(this);

        initViews();
        setupRecentTasksRecyclerView();
        setupListeners();
        updateHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        btnLogout = findViewById(R.id.btnLogout);

        cardTotalTasks = findViewById(R.id.cardTotalTasks);
        cardPendingTasks = findViewById(R.id.cardPendingTasks);
        cardInProgressTasks = findViewById(R.id.cardInProgressTasks);
        cardCompletedTasks = findViewById(R.id.cardCompletedTasks);

        tvStatTotalCount = findViewById(R.id.tvStatTotalCount);
        tvStatPendingCount = findViewById(R.id.tvStatPendingCount);
        tvStatInProgressCount = findViewById(R.id.tvStatInProgressCount);
        tvStatCompletedCount = findViewById(R.id.tvStatCompletedCount);

        btnViewAllTasks = findViewById(R.id.btnViewAllTasks);
        btnQuickAddTask = findViewById(R.id.btnQuickAddTask);
        tvSeeAllRecent = findViewById(R.id.tvSeeAllRecent);
        tvEmptyRecent = findViewById(R.id.tvEmptyRecent);
        rvRecentTasks = findViewById(R.id.rvRecentTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void updateHeaderInfo() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            tvGreeting.setText(getString(R.string.dashboard_greeting, user.getFullName()));
        }
        tvCurrentDate.setText(DateUtils.formatFullDate(new Date()));
    }

    private void setupRecentTasksRecyclerView() {
        recentTaskAdapter = new TaskAdapter(this);
        rvRecentTasks.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTasks.setAdapter(recentTaskAdapter);

        // Click task -> open TaskDetailActivity
        recentTaskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(DashboardActivity.this, TaskDetailActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);
        });

        // Edit task -> open AddEditTaskActivity
        recentTaskAdapter.setOnTaskEditClickListener(task -> {
            Intent intent = new Intent(DashboardActivity.this, AddEditTaskActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);
        });

        // Delete task with dialog
        recentTaskAdapter.setOnTaskDeleteClickListener(this::showDeleteConfirmationDialog);

        // Quick status click
        recentTaskAdapter.setOnTaskStatusClickListener(this::cycleTaskStatus);
    }

    private void setupListeners() {
        // KPI Card Clicks: Filter task list directly!
        cardTotalTasks.setOnClickListener(v -> navigateToTaskList(Constants.FILTER_ALL));
        cardPendingTasks.setOnClickListener(v -> navigateToTaskList(TaskStatus.PENDING.name()));
        cardInProgressTasks.setOnClickListener(v -> navigateToTaskList(TaskStatus.IN_PROGRESS.name()));
        cardCompletedTasks.setOnClickListener(v -> navigateToTaskList(TaskStatus.COMPLETED.name()));

        // Quick action buttons
        btnViewAllTasks.setOnClickListener(v -> navigateToTaskList(Constants.FILTER_ALL));
        tvSeeAllRecent.setOnClickListener(v -> navigateToTaskList(Constants.FILTER_ALL));

        btnQuickAddTask.setOnClickListener(v -> navigateToAddTask());
        fabAddTask.setOnClickListener(v -> navigateToAddTask());

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadDashboardData() {
        // 1. Load Metric Counts
        taskRepository.getDashboardStats(currentUserId, new TaskRepository.Callback<DatabaseHelper.DashboardStats>() {
            @Override
            public void onSuccess(DatabaseHelper.DashboardStats stats) {
                tvStatTotalCount.setText(String.valueOf(stats.totalTasks));
                tvStatPendingCount.setText(String.valueOf(stats.pendingTasks));
                tvStatInProgressCount.setText(String.valueOf(stats.inProgressTasks));
                tvStatCompletedCount.setText(String.valueOf(stats.completedTasks));
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(DashboardActivity.this, "Error loading statistics", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Load Recent Tasks (latest 3)
        taskRepository.getRecentTasks(currentUserId, 3, new TaskRepository.Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (tasks != null && !tasks.isEmpty()) {
                    tvEmptyRecent.setVisibility(View.GONE);
                    rvRecentTasks.setVisibility(View.VISIBLE);
                    recentTaskAdapter.setTasks(tasks);
                } else {
                    tvEmptyRecent.setVisibility(View.VISIBLE);
                    rvRecentTasks.setVisibility(View.GONE);
                    recentTaskAdapter.setTasks(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                tvEmptyRecent.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateToTaskList(String statusFilter) {
        Intent intent = new Intent(DashboardActivity.this, TaskListActivity.class);
        intent.putExtra(Constants.EXTRA_INITIAL_STATUS_FILTER, statusFilter);
        startActivity(intent);
    }

    private void navigateToAddTask() {
        Intent intent = new Intent(DashboardActivity.this, AddEditTaskActivity.class);
        startActivity(intent);
    }

    private void cycleTaskStatus(Task task, View anchorView) {
        TaskStatus nextStatus;
        if (task.getStatus() == TaskStatus.PENDING) {
            nextStatus = TaskStatus.IN_PROGRESS;
        } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            nextStatus = TaskStatus.COMPLETED;
        } else {
            nextStatus = TaskStatus.PENDING;
        }

        taskRepository.updateTaskStatus(task.getId(), nextStatus, new TaskRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(DashboardActivity.this,
                        getString(R.string.msg_status_updated, nextStatus.getDisplayName()),
                        Toast.LENGTH_SHORT).show();
                loadDashboardData();
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(DashboardActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, task.getTitle()))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    taskRepository.deleteTask(task.getId(), new TaskRepository.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(DashboardActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                            loadDashboardData();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(DashboardActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.action_logout_confirm, (dialog, which) -> {
                    sessionManager.logoutUser();
                    navigateToLogin();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
