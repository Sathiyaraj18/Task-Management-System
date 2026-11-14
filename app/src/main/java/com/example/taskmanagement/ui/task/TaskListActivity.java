package com.example.taskmanagement.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagement.R;
import com.example.taskmanagement.adapter.TaskAdapter;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.Constants;
import com.example.taskmanagement.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Task list screen featuring:
 * - RecyclerView for displaying tasks
 * - Real-time search by Title and Description
 * - Status filter chips (All, Pending, In Progress, Completed)
 * - Priority filter chips (All Priorities, High, Medium, Low)
 * - Sorting options (Due Date, Priority, Date Added)
 * - Edit, Delete, and Status update capabilities
 */
public class TaskListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearchQuery;
    private ImageButton btnClearSearch;

    // Status filter chips
    private TextView chipStatusAll;
    private TextView chipStatusPending;
    private TextView chipStatusInProgress;
    private TextView chipStatusCompleted;

    // Priority filter chips
    private TextView chipPriorityAll;
    private TextView chipPriorityHigh;
    private TextView chipPriorityMedium;
    private TextView chipPriorityLow;

    // Content Views
    private RecyclerView rvTaskList;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnEmptyCreateTask;
    private ProgressBar progressBarTasks;
    private FloatingActionButton fabAddTask;

    private TaskAdapter taskAdapter;
    private TaskRepository taskRepository;
    private SessionManager sessionManager;
    private long currentUserId;

    // Filter states
    private String currentStatusFilter = Constants.FILTER_ALL;
    private String currentPriorityFilter = Constants.FILTER_ALL;
    private String currentSearchQuery = "";
    private String currentSortBy = "due_date_asc"; // Default earliest due date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        taskRepository = new TaskRepository(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();

        // Check if intent passed an initial status filter from dashboard
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_INITIAL_STATUS_FILTER)) {
            String initialFilter = getIntent().getStringExtra(Constants.EXTRA_INITIAL_STATUS_FILTER);
            if (initialFilter != null && !initialFilter.isEmpty()) {
                currentStatusFilter = initialFilter;
            }
        }
        updateStatusChipVisuals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarTaskList);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        chipStatusAll = findViewById(R.id.chipStatusAll);
        chipStatusPending = findViewById(R.id.chipStatusPending);
        chipStatusInProgress = findViewById(R.id.chipStatusInProgress);
        chipStatusCompleted = findViewById(R.id.chipStatusCompleted);

        chipPriorityAll = findViewById(R.id.chipPriorityAll);
        chipPriorityHigh = findViewById(R.id.chipPriorityHigh);
        chipPriorityMedium = findViewById(R.id.chipPriorityMedium);
        chipPriorityLow = findViewById(R.id.chipPriorityLow);

        rvTaskList = findViewById(R.id.rvTaskList);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyCreateTask = findViewById(R.id.btnEmptyCreateTask);
        progressBarTasks = findViewById(R.id.progressBarTasks);
        fabAddTask = findViewById(R.id.fabAddTaskFromList);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        rvTaskList.setAdapter(taskAdapter);

        // Task card clicked -> view detail
        taskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);
        });

        // Edit button clicked
        taskAdapter.setOnTaskEditClickListener(task -> {
            Intent intent = new Intent(TaskListActivity.this, AddEditTaskActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);
        });

        // Delete button clicked
        taskAdapter.setOnTaskDeleteClickListener(this::showDeleteDialog);

        // Status badge clicked -> quick status update
        taskAdapter.setOnTaskStatusClickListener(this::showQuickStatusMenu);

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });

        btnEmptyCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        // Status Chips
        chipStatusAll.setOnClickListener(v -> {
            currentStatusFilter = Constants.FILTER_ALL;
            updateStatusChipVisuals();
            loadTasks();
        });

        chipStatusPending.setOnClickListener(v -> {
            currentStatusFilter = TaskStatus.PENDING.name();
            updateStatusChipVisuals();
            loadTasks();
        });

        chipStatusInProgress.setOnClickListener(v -> {
            currentStatusFilter = TaskStatus.IN_PROGRESS.name();
            updateStatusChipVisuals();
            loadTasks();
        });

        chipStatusCompleted.setOnClickListener(v -> {
            currentStatusFilter = TaskStatus.COMPLETED.name();
            updateStatusChipVisuals();
            loadTasks();
        });

        // Priority Chips
        chipPriorityAll.setOnClickListener(v -> {
            currentPriorityFilter = Constants.FILTER_ALL;
            updatePriorityChipVisuals();
            loadTasks();
        });

        chipPriorityHigh.setOnClickListener(v -> {
            currentPriorityFilter = Priority.HIGH.name();
            updatePriorityChipVisuals();
            loadTasks();
        });

        chipPriorityMedium.setOnClickListener(v -> {
            currentPriorityFilter = Priority.MEDIUM.name();
            updatePriorityChipVisuals();
            loadTasks();
        });

        chipPriorityLow.setOnClickListener(v -> {
            currentPriorityFilter = Priority.LOW.name();
            updatePriorityChipVisuals();
            loadTasks();
        });
    }

    private void updateStatusChipVisuals() {
        setChipState(chipStatusAll, currentStatusFilter.equalsIgnoreCase(Constants.FILTER_ALL));
        setChipState(chipStatusPending, currentStatusFilter.equalsIgnoreCase(TaskStatus.PENDING.name()));
        setChipState(chipStatusInProgress, currentStatusFilter.equalsIgnoreCase(TaskStatus.IN_PROGRESS.name()));
        setChipState(chipStatusCompleted, currentStatusFilter.equalsIgnoreCase(TaskStatus.COMPLETED.name()));
    }

    private void updatePriorityChipVisuals() {
        setChipState(chipPriorityAll, currentPriorityFilter.equalsIgnoreCase(Constants.FILTER_ALL));
        setChipState(chipPriorityHigh, currentPriorityFilter.equalsIgnoreCase(Priority.HIGH.name()));
        setChipState(chipPriorityMedium, currentPriorityFilter.equalsIgnoreCase(Priority.MEDIUM.name()));
        setChipState(chipPriorityLow, currentPriorityFilter.equalsIgnoreCase(Priority.LOW.name()));
    }

    private void setChipState(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void setupSearch() {
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearchQuery.setText("");
            currentSearchQuery = "";
            loadTasks();
        });
    }

    private void loadTasks() {
        progressBarTasks.setVisibility(View.VISIBLE);
        taskRepository.getFilteredTasks(
                currentUserId,
                currentSearchQuery,
                currentStatusFilter,
                currentPriorityFilter,
                currentSortBy,
                new TaskRepository.Callback<List<Task>>() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        progressBarTasks.setVisibility(View.GONE);
                        if (tasks != null && !tasks.isEmpty()) {
                            layoutEmptyState.setVisibility(View.GONE);
                            rvTaskList.setVisibility(View.VISIBLE);
                            taskAdapter.setTasks(tasks);
                        } else {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            rvTaskList.setVisibility(View.GONE);
                            taskAdapter.setTasks(null);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        progressBarTasks.setVisibility(View.GONE);
                        Toast.makeText(TaskListActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showQuickStatusMenu(Task task, View anchorView) {
        String[] statuses = new String[]{
                TaskStatus.PENDING.getDisplayName(),
                TaskStatus.IN_PROGRESS.getDisplayName(),
                TaskStatus.COMPLETED.getDisplayName()
        };

        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setItems(statuses, (dialog, which) -> {
                    TaskStatus selectedStatus;
                    switch (which) {
                        case 0:
                            selectedStatus = TaskStatus.PENDING;
                            break;
                        case 1:
                            selectedStatus = TaskStatus.IN_PROGRESS;
                            break;
                        case 2:
                        default:
                            selectedStatus = TaskStatus.COMPLETED;
                            break;
                    }

                    taskRepository.updateTaskStatus(task.getId(), selectedStatus, new TaskRepository.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(TaskListActivity.this,
                                    getString(R.string.msg_status_updated, selectedStatus.getDisplayName()),
                                    Toast.LENGTH_SHORT).show();
                            loadTasks();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(TaskListActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void showDeleteDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, task.getTitle()))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    taskRepository.deleteTask(task.getId(), new TaskRepository.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(TaskListActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                            loadTasks();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(TaskListActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortOptionsDialog() {
        String[] sortOptions = new String[]{
                getString(R.string.sort_by_due_date),
                getString(R.string.sort_by_due_date_desc),
                getString(R.string.sort_by_priority),
                getString(R.string.sort_by_newest)
        };

        int selectedIndex = 0;
        if ("due_date_desc".equalsIgnoreCase(currentSortBy)) selectedIndex = 1;
        else if ("priority".equalsIgnoreCase(currentSortBy)) selectedIndex = 2;
        else if ("newest".equalsIgnoreCase(currentSortBy)) selectedIndex = 3;

        new AlertDialog.Builder(this)
                .setTitle("Sort Tasks")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentSortBy = "due_date_asc";
                            break;
                        case 1:
                            currentSortBy = "due_date_desc";
                            break;
                        case 2:
                            currentSortBy = "priority";
                            break;
                        case 3:
                            currentSortBy = "newest";
                            break;
                    }
                    dialog.dismiss();
                    loadTasks();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
