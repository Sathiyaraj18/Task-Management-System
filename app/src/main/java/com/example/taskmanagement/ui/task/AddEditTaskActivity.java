package com.example.taskmanagement.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanagement.R;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.Constants;
import com.example.taskmanagement.util.DateUtils;
import com.example.taskmanagement.util.SessionManager;
import com.example.taskmanagement.util.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Screen for Creating a new task or Editing an existing task,
 * with DatePickerDialog pickers for Start Date and Due Date,
 * Priority and Status spinners, and deletion support in Edit mode.
 */
public class AddEditTaskActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout tilTaskTitle;
    private TextInputLayout tilTaskDescription;
    private TextInputLayout tilStartDate;
    private TextInputLayout tilDueDate;

    private TextInputEditText etTaskTitle;
    private TextInputEditText etTaskDescription;
    private TextInputEditText etStartDate;
    private TextInputEditText etDueDate;

    private Spinner spinnerPriority;
    private Spinner spinnerStatus;

    private MaterialButton btnSaveTask;
    private MaterialButton btnDeleteTask;

    private TaskRepository taskRepository;
    private SessionManager sessionManager;
    private long currentUserId;

    private Task existingTask;
    private boolean isEditMode = false;

    private final Calendar calendarStart = Calendar.getInstance();
    private final Calendar calendarDue = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.FORMAT_DATABASE, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        taskRepository = new TaskRepository(this);

        initViews();
        setupSpinners();
        checkIntentData();
        setupToolbar();
        setupDatePickers();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarAddEditTask);
        tilTaskTitle = findViewById(R.id.tilTaskTitle);
        tilTaskDescription = findViewById(R.id.tilTaskDescription);
        tilStartDate = findViewById(R.id.tilStartDate);
        tilDueDate = findViewById(R.id.tilDueDate);

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etDueDate = findViewById(R.id.etDueDate);

        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
    }

    private void setupSpinners() {
        // Priority Spinner options
        String[] priorities = new String[]{
                Priority.HIGH.getDisplayName(),
                Priority.MEDIUM.getDisplayName(),
                Priority.LOW.getDisplayName()
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default Medium

        // Status Spinner options
        String[] statuses = new String[]{
                TaskStatus.PENDING.getDisplayName(),
                TaskStatus.IN_PROGRESS.getDisplayName(),
                TaskStatus.COMPLETED.getDisplayName()
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(0); // Default Pending
    }

    private void checkIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_TASK)) {
            existingTask = (Task) getIntent().getSerializableExtra(Constants.EXTRA_TASK);
            if (existingTask != null) {
                isEditMode = true;
                populateExistingData();
            }
        }

        if (!isEditMode) {
            // Default dates for new task: Start = today, Due = today + 3 days
            calendarDue.add(Calendar.DAY_OF_YEAR, 3);
            etStartDate.setText(dateFormat.format(calendarStart.getTime()));
            etDueDate.setText(dateFormat.format(calendarDue.getTime()));
        }
    }

    private void populateExistingData() {
        etTaskTitle.setText(existingTask.getTitle());
        etTaskDescription.setText(existingTask.getDescription());
        etStartDate.setText(existingTask.getStartDate());
        etDueDate.setText(existingTask.getDueDate());

        // Parse calendar dates for pickers
        Date startDate = DateUtils.parseDate(existingTask.getStartDate());
        if (startDate != null) calendarStart.setTime(startDate);

        Date dueDate = DateUtils.parseDate(existingTask.getDueDate());
        if (dueDate != null) calendarDue.setTime(dueDate);

        // Priority
        switch (existingTask.getPriority()) {
            case HIGH:
                spinnerPriority.setSelection(0);
                break;
            case MEDIUM:
                spinnerPriority.setSelection(1);
                break;
            case LOW:
                spinnerPriority.setSelection(2);
                break;
        }

        // Status
        switch (existingTask.getStatus()) {
            case PENDING:
                spinnerStatus.setSelection(0);
                break;
            case IN_PROGRESS:
                spinnerStatus.setSelection(1);
                break;
            case COMPLETED:
                spinnerStatus.setSelection(2);
                break;
        }

        btnSaveTask.setText(R.string.btn_update_task);
        btnDeleteTask.setVisibility(View.VISIBLE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? R.string.title_edit_task : R.string.title_create_task);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDatePickers() {
        // Start Date Picker Dialog
        View.OnClickListener startPickerListener = v -> {
            int year = calendarStart.get(Calendar.YEAR);
            int month = calendarStart.get(Calendar.MONTH);
            int day = calendarStart.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
                calendarStart.set(Calendar.YEAR, y);
                calendarStart.set(Calendar.MONTH, m);
                calendarStart.set(Calendar.DAY_OF_MONTH, d);
                etStartDate.setText(dateFormat.format(calendarStart.getTime()));
                tilStartDate.setError(null);
            }, year, month, day);
            dialog.show();
        };

        etStartDate.setOnClickListener(startPickerListener);
        tilStartDate.setEndIconOnClickListener(startPickerListener);

        // Due Date Picker Dialog
        View.OnClickListener duePickerListener = v -> {
            int year = calendarDue.get(Calendar.YEAR);
            int month = calendarDue.get(Calendar.MONTH);
            int day = calendarDue.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
                calendarDue.set(Calendar.YEAR, y);
                calendarDue.set(Calendar.MONTH, m);
                calendarDue.set(Calendar.DAY_OF_MONTH, d);
                etDueDate.setText(dateFormat.format(calendarDue.getTime()));
                tilDueDate.setError(null);
            }, year, month, day);
            dialog.show();
        };

        etDueDate.setOnClickListener(duePickerListener);
        tilDueDate.setEndIconOnClickListener(duePickerListener);
    }

    private void setupListeners() {
        btnSaveTask.setOnClickListener(v -> saveTask());

        btnDeleteTask.setOnClickListener(v -> {
            if (existingTask != null) {
                showDeleteDialog();
            }
        });
    }

    private void saveTask() {
        String title = etTaskTitle.getText() != null ? etTaskTitle.getText().toString().trim() : "";
        String description = etTaskDescription.getText() != null ? etTaskDescription.getText().toString().trim() : "";
        String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String dueDate = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        // Reset errors
        tilTaskTitle.setError(null);
        tilStartDate.setError(null);
        tilDueDate.setError(null);

        boolean isValid = true;

        if (ValidationUtils.isEmpty(title)) {
            tilTaskTitle.setError(getString(R.string.error_task_title_required));
            isValid = false;
        }

        if (ValidationUtils.isEmpty(startDate)) {
            tilStartDate.setError(getString(R.string.error_start_date_required));
            isValid = false;
        }

        if (ValidationUtils.isEmpty(dueDate)) {
            tilDueDate.setError(getString(R.string.error_due_date_required));
            isValid = false;
        }

        if (isValid && DateUtils.isDueDateBeforeStartDate(startDate, dueDate)) {
            tilDueDate.setError(getString(R.string.error_due_date_before_start));
            isValid = false;
        }

        if (!isValid) return;

        // Selected Priority
        Priority priority;
        int pIndex = spinnerPriority.getSelectedItemPosition();
        if (pIndex == 0) priority = Priority.HIGH;
        else if (pIndex == 1) priority = Priority.MEDIUM;
        else priority = Priority.LOW;

        // Selected Status
        TaskStatus status;
        int sIndex = spinnerStatus.getSelectedItemPosition();
        if (sIndex == 0) status = TaskStatus.PENDING;
        else if (sIndex == 1) status = TaskStatus.IN_PROGRESS;
        else status = TaskStatus.COMPLETED;

        btnSaveTask.setEnabled(false);

        if (isEditMode) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setStartDate(startDate);
            existingTask.setDueDate(dueDate);
            existingTask.setPriority(priority);
            existingTask.setStatus(status);

            taskRepository.updateTask(existingTask, new TaskRepository.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    btnSaveTask.setEnabled(true);
                    Toast.makeText(AddEditTaskActivity.this, R.string.msg_task_updated, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(Throwable throwable) {
                    btnSaveTask.setEnabled(true);
                    Toast.makeText(AddEditTaskActivity.this, "Failed to update: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Task newTask = new Task(currentUserId, title, description, startDate, dueDate, priority, status);
            taskRepository.insertTask(newTask, new TaskRepository.Callback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    btnSaveTask.setEnabled(true);
                    Toast.makeText(AddEditTaskActivity.this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(Throwable throwable) {
                    btnSaveTask.setEnabled(true);
                    Toast.makeText(AddEditTaskActivity.this, "Failed to create task: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, existingTask.getTitle()))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    taskRepository.deleteTask(existingTask.getId(), new TaskRepository.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(AddEditTaskActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(AddEditTaskActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
