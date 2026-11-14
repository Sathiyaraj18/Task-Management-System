package com.example.taskmanagement.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.taskmanagement.data.DatabaseContract.TaskEntry;
import com.example.taskmanagement.data.DatabaseContract.UserEntry;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQLiteOpenHelper singleton implementation managing database lifecycle and CRUD operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        db.execSQL(UserEntry.SQL_CREATE_TABLE);
        db.execSQL(TaskEntry.SQL_CREATE_TABLE);

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL(TaskEntry.SQL_DROP_TABLE);
        db.execSQL(UserEntry.SQL_DROP_TABLE);
        onCreate(db);
    }

    /**
     * Seeds initial sample user and tasks so the app is ready for demonstration out of the box.
     */
    private void seedDefaultData(SQLiteDatabase db) {
        try {
            long now = System.currentTimeMillis();

            // Seed default admin user: username=admin, password=password123
            ContentValues userValues = new ContentValues();
            userValues.put(UserEntry.COLUMN_USERNAME, "admin");
            userValues.put(UserEntry.COLUMN_PASSWORD, "password123");
            userValues.put(UserEntry.COLUMN_FULL_NAME, "Alex Morgan");
            userValues.put(UserEntry.COLUMN_EMAIL, "alex.morgan@example.com");
            userValues.put(UserEntry.COLUMN_CREATED_AT, now);
            long adminId = db.insert(UserEntry.TABLE_NAME, null, userValues);

            if (adminId <= 0) {
                adminId = 1;
            }

            // Calculate sample dates (today, tomorrow, next week, past date)
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_DATABASE, Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            String todayStr = sdf.format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, 3);
            String plusThreeDays = sdf.format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, 5);
            String nextWeekStr = sdf.format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -10);
            String pastWeekStr = sdf.format(cal.getTime());

            // Seed realistic tasks
            seedTask(db, adminId, "Complete Architecture Design Document",
                    "Finalize system architecture diagrams and technical specifications for the upcoming Q4 mobile release.",
                    todayStr, plusThreeDays, Priority.HIGH, TaskStatus.IN_PROGRESS);

            seedTask(db, adminId, "Setup Room / SQLite Database Migrations",
                    "Implement SQLiteOpenHelper schemas, ensure proper indexing and contract constants.",
                    pastWeekStr, todayStr, Priority.HIGH, TaskStatus.COMPLETED);

            seedTask(db, adminId, "Design User Dashboard UI Components",
                    "Build responsive summary cards for Total, Pending, In Progress, and Completed task counters.",
                    todayStr, plusThreeDays, Priority.MEDIUM, TaskStatus.IN_PROGRESS);

            seedTask(db, adminId, "Implement Search and Status Filters",
                    "Add real-time filter chips and search bar in task list RecyclerView.",
                    todayStr, nextWeekStr, Priority.MEDIUM, TaskStatus.PENDING);

            seedTask(db, adminId, "QA Testing & Bug Fixes",
                    "Conduct unit tests and end-to-end user navigation flow verification.",
                    plusThreeDays, nextWeekStr, Priority.LOW, TaskStatus.PENDING);

            Log.d(TAG, "Sample data seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding default data", e);
        }
    }

    private void seedTask(SQLiteDatabase db, long userId, String title, String description,
                           String startDate, String dueDate, Priority priority, TaskStatus status) {
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_USER_ID, userId);
        values.put(TaskEntry.COLUMN_TITLE, title);
        values.put(TaskEntry.COLUMN_DESCRIPTION, description);
        values.put(TaskEntry.COLUMN_START_DATE, startDate);
        values.put(TaskEntry.COLUMN_DUE_DATE, dueDate);
        values.put(TaskEntry.COLUMN_PRIORITY, priority.name());
        values.put(TaskEntry.COLUMN_STATUS, status.name());
        values.put(TaskEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(TaskEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TaskEntry.TABLE_NAME, null, values);
    }

    // =========================================================================
    // TASK CRUD OPERATIONS
    // =========================================================================

    /**
     * Insert a new task into the database.
     */
    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_USER_ID, task.getUserId());
        values.put(TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskEntry.COLUMN_START_DATE, task.getStartDate());
        values.put(TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskEntry.COLUMN_PRIORITY, task.getPriority().name());
        values.put(TaskEntry.COLUMN_STATUS, task.getStatus().name());
        long now = System.currentTimeMillis();
        values.put(TaskEntry.COLUMN_CREATED_AT, now);
        values.put(TaskEntry.COLUMN_UPDATED_AT, now);

        return db.insert(TaskEntry.TABLE_NAME, null, values);
    }

    /**
     * Update an existing task.
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskEntry.COLUMN_START_DATE, task.getStartDate());
        values.put(TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskEntry.COLUMN_PRIORITY, task.getPriority().name());
        values.put(TaskEntry.COLUMN_STATUS, task.getStatus().name());
        values.put(TaskEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(TaskEntry.TABLE_NAME, values,
                TaskEntry._ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    /**
     * Quick status update for a task.
     */
    public int updateTaskStatus(long taskId, TaskStatus newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_STATUS, newStatus.name());
        values.put(TaskEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(TaskEntry.TABLE_NAME, values,
                TaskEntry._ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    /**
     * Delete a task by ID.
     */
    public int deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TaskEntry.TABLE_NAME,
                TaskEntry._ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    /**
     * Fetch a task by its primary key ID.
     */
    public Task getTaskById(long taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry._ID + " = ?",
                    new String[]{String.valueOf(taskId)},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return parseTaskFromCursor(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * Fetch all tasks for a specific user.
     */
    public List<Task> getAllTasks(long userId) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    TaskEntry.COLUMN_CREATED_AT + " DESC"
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseTaskFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Query tasks with search text, status filter, priority filter, and sort order.
     */
    public List<Task> getFilteredTasks(long userId, String query, String statusFilter,
                                       String priorityFilter, String sortBy) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selection = new StringBuilder(TaskEntry.COLUMN_USER_ID + " = ?");
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(userId));

        // Filter by Status
        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            selection.append(" AND ").append(TaskEntry.COLUMN_STATUS).append(" = ?");
            selectionArgs.add(statusFilter);
        }

        // Filter by Priority
        if (priorityFilter != null && !priorityFilter.isEmpty() && !priorityFilter.equalsIgnoreCase("ALL")) {
            selection.append(" AND ").append(TaskEntry.COLUMN_PRIORITY).append(" = ?");
            selectionArgs.add(priorityFilter);
        }

        // Search Query (matches title or description)
        if (query != null && !query.trim().isEmpty()) {
            selection.append(" AND (").append(TaskEntry.COLUMN_TITLE).append(" LIKE ? OR ")
                    .append(TaskEntry.COLUMN_DESCRIPTION).append(" LIKE ?)");
            String wildcard = "%" + query.trim() + "%";
            selectionArgs.add(wildcard);
            selectionArgs.add(wildcard);
        }

        // Determine Sort Order
        String orderBy;
        if ("due_date_asc".equalsIgnoreCase(sortBy)) {
            orderBy = TaskEntry.COLUMN_DUE_DATE + " ASC";
        } else if ("due_date_desc".equalsIgnoreCase(sortBy)) {
            orderBy = TaskEntry.COLUMN_DUE_DATE + " DESC";
        } else if ("priority".equalsIgnoreCase(sortBy)) {
            // High first, then Medium, Low
            orderBy = "CASE " + TaskEntry.COLUMN_PRIORITY +
                    " WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC";
        } else {
            // Default newest first
            orderBy = TaskEntry.COLUMN_CREATED_AT + " DESC";
        }

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TaskEntry.TABLE_NAME,
                    null,
                    selection.toString(),
                    selectionArgs.toArray(new String[0]),
                    null, null,
                    orderBy
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseTaskFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Returns dashboard count statistics: Total, Pending, In Progress, Completed.
     */
    public DashboardStats getDashboardStats(long userId) {
        DashboardStats stats = new DashboardStats();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT " + TaskEntry.COLUMN_STATUS + ", COUNT(*) FROM " + TaskEntry.TABLE_NAME +
                " WHERE " + TaskEntry.COLUMN_USER_ID + " = ?" +
                " GROUP BY " + TaskEntry.COLUMN_STATUS;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String statusName = cursor.getString(0);
                    int count = cursor.getInt(1);
                    stats.totalTasks += count;

                    TaskStatus status = TaskStatus.fromString(statusName);
                    switch (status) {
                        case PENDING:
                            stats.pendingTasks += count;
                            break;
                        case IN_PROGRESS:
                            stats.inProgressTasks += count;
                            break;
                        case COMPLETED:
                            stats.completedTasks += count;
                            break;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return stats;
    }

    /**
     * Fetch recent tasks (e.g. latest 3 tasks) for dashboard display.
     */
    public List<Task> getRecentTasks(long userId, int limit) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    TaskEntry.COLUMN_CREATED_AT + " DESC",
                    String.valueOf(limit)
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(parseTaskFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    private Task parseTaskFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry._ID));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_USER_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_TITLE));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_DESCRIPTION));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_START_DATE));
        String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_DUE_DATE));
        String priorityStr = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_PRIORITY));
        String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_STATUS));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_CREATED_AT));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_UPDATED_AT));

        Priority priority = Priority.fromString(priorityStr);
        TaskStatus status = TaskStatus.fromString(statusStr);

        return new Task(id, userId, title, desc, startDate, dueDate, priority, status, createdAt, updatedAt);
    }

    // =========================================================================
    // USER OPERATIONS
    // =========================================================================

    /**
     * Register a new user in the local database.
     */
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, user.getUsername().trim().toLowerCase(Locale.ROOT));
        values.put(UserEntry.COLUMN_PASSWORD, user.getPassword());
        values.put(UserEntry.COLUMN_FULL_NAME, user.getFullName());
        values.put(UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(UserEntry.COLUMN_CREATED_AT, System.currentTimeMillis());

        return db.insert(UserEntry.TABLE_NAME, null, values);
    }

    /**
     * Authenticate user credentials.
     */
    public User authenticateUser(String username, String password) {
        if (username == null || password == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    UserEntry.TABLE_NAME,
                    null,
                    UserEntry.COLUMN_USERNAME + " = ? AND " + UserEntry.COLUMN_PASSWORD + " = ?",
                    new String[]{username.trim().toLowerCase(Locale.ROOT), password},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return parseUserFromCursor(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * Check if a username is already taken.
     */
    public boolean checkUsernameExists(String username) {
        if (username == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    UserEntry.TABLE_NAME,
                    new String[]{UserEntry._ID},
                    UserEntry.COLUMN_USERNAME + " = ?",
                    new String[]{username.trim().toLowerCase(Locale.ROOT)},
                    null, null, null
            );
            return (cursor != null && cursor.getCount() > 0);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private User parseUserFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry._ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_USERNAME));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_PASSWORD));
        String fullName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_FULL_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CREATED_AT));

        return new User(id, username, password, fullName, email, createdAt);
    }

    /**
     * Value object representing the dashboard metrics.
     */
    public static class DashboardStats {
        public int totalTasks = 0;
        public int pendingTasks = 0;
        public int inProgressTasks = 0;
        public int completedTasks = 0;
    }
}
