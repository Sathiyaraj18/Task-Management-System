package com.example.taskmanagement.data;

import android.provider.BaseColumns;

/**
 * Defines database schema, table names, and column identifiers.
 */
public final class DatabaseContract {

    private DatabaseContract() {
    }

    public static final String DATABASE_NAME = "task_management.db";
    public static final int DATABASE_VERSION = 1;

    /**
     * Inner class that defines the Tasks table contents.
     */
    public static final class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_DUE_DATE = "due_date";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_START_DATE + " TEXT NOT NULL, " +
                        COLUMN_DUE_DATE + " TEXT NOT NULL, " +
                        COLUMN_PRIORITY + " TEXT NOT NULL, " +
                        COLUMN_STATUS + " TEXT NOT NULL, " +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL" +
                        ");";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Inner class that defines the Users table contents.
     */
    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_FULL_NAME + " TEXT, " +
                        COLUMN_EMAIL + " TEXT, " +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL" +
                        ");";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
