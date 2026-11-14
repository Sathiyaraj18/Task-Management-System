package com.example.taskmanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanagement.R;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.ui.dashboard.DashboardActivity;
import com.example.taskmanagement.util.SessionManager;
import com.example.taskmanagement.util.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Account registration screen with input validation and local SQLite storage.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilRegUsername;
    private TextInputLayout tilEmail;
    private TextInputLayout tilRegPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFullName;
    private TextInputEditText etRegUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etRegPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnRegister;
    private TextView tvGoToLogin;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilRegUsername = findViewById(R.id.tilRegUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilRegPassword = findViewById(R.id.tilRegPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etRegUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegistration() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Reset errors
        tilFullName.setError(null);
        tilRegUsername.setError(null);
        tilEmail.setError(null);
        tilRegPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;

        if (ValidationUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_required_field));
            isValid = false;
        }

        if (ValidationUtils.isEmpty(username)) {
            tilRegUsername.setError(getString(R.string.error_required_field));
            isValid = false;
        } else if (!ValidationUtils.isValidUsername(username)) {
            tilRegUsername.setError(getString(R.string.error_invalid_username));
            isValid = false;
        }

        if (!ValidationUtils.isEmpty(email) && !ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (ValidationUtils.isEmpty(password)) {
            tilRegPassword.setError(getString(R.string.error_required_field));
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            tilRegPassword.setError(getString(R.string.error_invalid_password));
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        if (!isValid) return;

        btnRegister.setEnabled(false);
        User newUser = new User(username, password, fullName, email);

        userRepository.register(newUser, new UserRepository.Callback<Long>() {
            @Override
            public void onSuccess(Long newUserId) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                sessionManager.createLoginSession(newUser);

                Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Throwable throwable) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
