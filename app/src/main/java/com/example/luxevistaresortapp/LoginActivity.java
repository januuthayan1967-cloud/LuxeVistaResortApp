package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerText;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private ProgressBar loginProgressBar;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);

        sessionManager = SessionManager.getInstance(this);
        // If already logged in, navigate straight to HomeActivity
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_loginactivity);
        EdgeToEdgeHelper.applyRootWindowInsets(findViewById(R.id.loginScrollView));

        // Initialize views
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        registerText = findViewById(R.id.registerText);

        dbHelper = DBHelper.getInstance(this);

        // Clear errors seamlessly on text change
        setupRealTimeErrorClearing();

        // Keyboard "Done" action on password field triggers login
        passwordField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });

        // Login button action
        loginButton.setOnClickListener(v -> performLogin());

        // Register text action
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupRealTimeErrorClearing() {
        emailField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailInputLayout != null && emailInputLayout.getError() != null) {
                    emailInputLayout.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInputLayout != null && passwordInputLayout.getError() != null) {
                    passwordInputLayout.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void performLogin() {
        String email = emailField.getText() != null ? emailField.getText().toString().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().toString() : "";

        // Inline validation
        boolean hasError = false;

        if (email.isEmpty()) {
            if (emailInputLayout != null) {
                emailInputLayout.setError("Please enter your email address");
            }
            emailField.requestFocus();
            hasError = true;
        } else if (!SecurityUtils.isValidEmail(email)) {
            if (emailInputLayout != null) {
                emailInputLayout.setError("Please enter a valid email address");
            }
            emailField.requestFocus();
            hasError = true;
        }

        if (password.isEmpty()) {
            if (passwordInputLayout != null) {
                passwordInputLayout.setError("Please enter your password");
            }
            if (!hasError) {
                passwordField.requestFocus();
            }
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(LoginActivity.this, "Please check the highlighted fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show luxury loading state and disable button to prevent double submissions
        setLoadingState(true);

        UserModel user = dbHelper.authenticateUser(email, password);
        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getName(), user.getEmail());
            Toast.makeText(LoginActivity.this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            setLoadingState(false);
            if (passwordInputLayout != null) {
                passwordInputLayout.setError("Invalid credentials");
            }
            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (loginButton != null) {
            loginButton.setEnabled(!isLoading);
            loginButton.setText(isLoading ? "" : "Sign In");
        }
        if (loginProgressBar != null) {
            loginProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
