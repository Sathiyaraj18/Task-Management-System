package com.example.taskmanagement.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.taskmanagement.R;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.Constants;
import com.example.taskmanagement.util.DateUtils;
import com.google.android.material.button.MaterialButton;

/**
 * Detailed view of a single task with timeline, status indicator,
 * 1-click status update actions, and edit/delete capabilities.
 */
public class TaskDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvDetailPriority;
    private TextView tvDetailStatus;
    private TextView tvDetailOverdue;
    private TextView tvDetailTitle;
    private TextView tvDetailDescription;
    private TextView tvDetailStartDate;
    private TextView tvDetailDueDate;

    private MaterialButton btnSetPending;
    private MaterialButton btnSetInProgress;
    private MaterialButton btnSetCompleted;

    private MaterialButton btnEditTask;
    private MaterialButton btnDeleteTask;

    private TaskRepository taskRepository;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskRepository = new TaskRepository(this);

        if (getIntent() == null || !getIntent().hasExtra(Constants.EXTRA_TASK)) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        task = (Task) getIntent().getSerializableExtra(Constants.EXTRA_TASK);

        initViews();
        setupToolbar();
        populateTaskDetails();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload fresh task data from database if edited
        if (task != null) {
            taskRepository.getTaskById(task.getId(), new TaskRepository.Callback<Task>() {
                @Override
                public void onSuccess(Task updatedTask) {
                    if (updatedTask != null) {
                        task = updatedTask;
                        populateTaskDetails();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                }
            });
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarTaskDetail);
        tvDetailPriority = findViewById(R.id.tvDetailPriority);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailOverdue = findViewById(R.id.tvDetailOverdue);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailStartDate = findViewById(R.id.tvDetailStartDate);
        tvDetailDueDate = findViewById(R.id.tvDetailDueDate);

        btnSetPending = findViewById(R.id.btnSetPending);
        btnSetInProgress = findViewById(R.id.btnSetInProgress);
        btnSetCompleted = findViewById(R.id.btnSetCompleted);

        btnEditTask = findViewById(R.id.btnEditTaskFromDetail);
        btnDeleteTask = findViewById(R.id.btnDeleteTaskFromDetail);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void populateTaskDetails() {
        if (task == null) return;

        tvDetailTitle.setText(task.getTitle());
        tvDetailDescription.setText(task.getDescription() != null && !task.getDescription().trim().isEmpty()
                ? task.getDescription() : "No additional description provided.");

        tvDetailStartDate.setText(DateUtils.formatDisplayDate(task.getStartDate()));
        tvDetailDueDate.setText(DateUtils.formatDisplayDate(task.getDueDate()));

        // Priority
        Priority priority = task.getPriority();
        tvDetailPriority.setText(priority.getDisplayName());
        switch (priority) {
            case HIGH:
                tvDetailPriority.setBackgroundResource(R.drawable.badge_priority_high);
                tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_high_text));
                break;
            case MEDIUM:
                tvDetailPriority.setBackgroundResource(R.drawable.badge_priority_medium);
                tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_medium_text));
                break;
            case LOW:
            default:
                tvDetailPriority.setBackgroundResource(R.drawable.badge_priority_low);
                tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_low_text));
                break;
        }

        // Status
        updateStatusBadge(task.getStatus());

        // Overdue
        if (task.isOverdue()) {
            tvDetailOverdue.setVisibility(View.VISIBLE);
            long days = Math.abs(DateUtils.getDaysRemaining(task.getDueDate()));
            tvDetailOverdue.setText(days > 0 ? days + "d Overdue" : "Overdue");
        } else {
            tvDetailOverdue.setVisibility(View.GONE);
        }
    }

    private void updateStatusBadge(TaskStatus status) {
        tvDetailStatus.setText(status.getDisplayName());
        switch (status) {
            case PENDING:
                tvDetailStatus.setBackgroundResource(R.drawable.badge_pending);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending_text));
                break;
            case IN_PROGRESS:
                tvDetailStatus.setBackgroundResource(R.drawable.badge_in_progress);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_in_progress_text));
                break;
            case COMPLETED:
                tvDetailStatus.setBackgroundResource(R.drawable.badge_completed);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
                break;
        }
    }

    private void setupListeners() {
        // Quick 1-tap Status updates
        btnSetPending.setOnClickListener(v -> updateStatus(TaskStatus.PENDING));
        btnSetInProgress.setOnClickListener(v -> updateStatus(TaskStatus.IN_PROGRESS));
        btnSetCompleted.setOnClickListener(v -> updateStatus(TaskStatus.COMPLETED));

        // Edit Task
        btnEditTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailActivity.this, AddEditTaskActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);
        });

        // Delete Task
        btnDeleteTask.setOnClickListener(v -> showDeleteDialog());
    }

    private void updateStatus(TaskStatus newStatus) {
        if (task.getStatus() == newStatus) return;

        taskRepository.updateTaskStatus(task.getId(), newStatus, new TaskRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                task.setStatus(newStatus);
                updateStatusBadge(newStatus);
                tvDetailOverdue.setVisibility(task.isOverdue() ? View.VISIBLE : View.GONE);
                Toast.makeText(TaskDetailActivity.this,
                        getString(R.string.msg_status_updated, newStatus.getDisplayName()),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(TaskDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, task.getTitle()))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    taskRepository.deleteTask(task.getId(), new TaskRepository.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(TaskDetailActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(TaskDetailActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
