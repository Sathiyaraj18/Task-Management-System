package com.example.taskmanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
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
 * Login screen with username and password validation, local authentication,
 * and demo credentials helper.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private Button btnDemoCredentials;
    private TextView tvGoToRegister;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnDemoCredentials = findViewById(R.id.btnDemoCredentials);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void setupListeners() {
        // Clear errors on typing
        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilUsername.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
        });

        // Quick demo credentials
        btnDemoCredentials.setOnClickListener(v -> {
            etUsername.setText("admin");
            etPassword.setText("password123");
            tilUsername.setError(null);
            tilPassword.setError(null);
            Toast.makeText(this, "Demo credentials filled", Toast.LENGTH_SHORT).show();
        });

        // Login Action
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigation to Register
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean isValid = true;

        if (ValidationUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.error_required_field));
            isValid = false;
        } else if (!ValidationUtils.isValidUsername(username)) {
            tilUsername.setError(getString(R.string.error_invalid_username));
            isValid = false;
        }

        if (ValidationUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_required_field));
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.error_invalid_password));
            isValid = false;
        }

        if (!isValid) return;

        btnLogin.setEnabled(false);
        userRepository.authenticate(username, password, new UserRepository.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);
                if (user != null) {
                    sessionManager.createLoginSession(user);
                    Toast.makeText(LoginActivity.this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    tilPassword.setError("Invalid username or password");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Authentication error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
