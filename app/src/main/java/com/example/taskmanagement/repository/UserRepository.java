package com.example.taskmanagement.repository;

import android.content.Context;

import com.example.taskmanagement.data.DatabaseHelper;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.util.AppExecutors;

/**
 * Repository handling user registration, authentication, and profile checks.
 */
public class UserRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }

    private final DatabaseHelper dbHelper;
    private final AppExecutors executors;

    public UserRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executors = AppExecutors.getInstance();
    }

    public void authenticate(String username, String password, Callback<User> callback) {
        executors.diskIO().execute(() -> {
            try {
                User user = dbHelper.authenticateUser(username, password);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(user);
                });
            } catch (Exception e) {
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e);
                });
            }
        });
    }

    public void register(User user, Callback<Long> callback) {
        executors.diskIO().execute(() -> {
            try {
                if (dbHelper.checkUsernameExists(user.getUsername())) {
                    executors.mainThread().execute(() -> {
                        if (callback != null) {
                            callback.onError(new Exception("Username is already taken. Please choose another."));
                        }
                    });
                    return;
                }

                long id = dbHelper.insertUser(user);
                executors.mainThread().execute(() -> {
                    if (callback != null) {
                        if (id > 0) {
                            user.setId(id);
                            callback.onSuccess(id);
                        } else {
                            callback.onError(new Exception("Registration failed. Please try again."));
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
}
