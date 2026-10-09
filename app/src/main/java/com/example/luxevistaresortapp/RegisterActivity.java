package com.example.luxevistaresortapp;

import android.content.Intent;
import android.content.res.ColorStateList;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerName, registerEmail, registerPassword, registerConfirmPassword;
    private Button registerButton;
    private TextView loginText;
    private TextInputLayout registerNameLayout, registerEmailLayout, registerPasswordLayout, registerConfirmPasswordLayout;
    private ProgressBar passwordStrengthProgress, registerProgressBar;
    private TextView tvPasswordStrength, tvPasswordMatch;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_activityregister);

        EdgeToEdgeHelper.applyRootWindowInsets(findViewById(R.id.registerScrollView));

        // Link the UI components
        registerNameLayout = findViewById(R.id.registerNameLayout);
        registerEmailLayout = findViewById(R.id.registerEmailLayout);
        registerPasswordLayout = findViewById(R.id.registerPasswordLayout);
        registerConfirmPasswordLayout = findViewById(R.id.registerConfirmPasswordLayout);

        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword);

        passwordStrengthProgress = findViewById(R.id.passwordStrengthProgress);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        tvPasswordMatch = findViewById(R.id.tvPasswordMatch);

        registerButton = findViewById(R.id.registerButton);
        registerProgressBar = findViewById(R.id.registerProgressBar);
        loginText = findViewById(R.id.loginText);

        dbHelper = DBHelper.getInstance(this);

        setupRealTimeListeners();

        // Keyboard "Done" action on confirm password triggers registration
        registerConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performRegistration();
                return true;
            }
            return false;
        });

        registerButton.setOnClickListener(v -> performRegistration());

        // Navigate to Login Activity if user already has an account
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupRealTimeListeners() {
        registerName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (registerNameLayout != null && registerNameLayout.getError() != null) {
                    registerNameLayout.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        registerEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (registerEmailLayout != null && registerEmailLayout.getError() != null) {
                    registerEmailLayout.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        registerPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (registerPasswordLayout != null && registerPasswordLayout.getError() != null) {
                    registerPasswordLayout.setError(null);
                }
                updatePasswordStrength(s != null ? s.toString() : "");
                updatePasswordMatchGuidance();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        registerConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (registerConfirmPasswordLayout != null && registerConfirmPasswordLayout.getError() != null) {
                    registerConfirmPasswordLayout.setError(null);
                }
                updatePasswordMatchGuidance();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthProgress == null || tvPasswordStrength == null) return;

        SecurityUtils.PasswordStrength strength = SecurityUtils.evaluatePasswordStrength(password);
        switch (strength) {
            case EMPTY:
                passwordStrengthProgress.setProgress(0);
                tvPasswordStrength.setText("Requirement: Minimum 6 characters");
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                break;
            case TOO_SHORT:
                passwordStrengthProgress.setProgress(30);
                passwordStrengthProgress.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.strength_weak)));
                tvPasswordStrength.setText("Too short (minimum 6 characters required)");
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_weak));
                break;
            case MODERATE:
                passwordStrengthProgress.setProgress(65);
                passwordStrengthProgress.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.strength_fair)));
                tvPasswordStrength.setText("Good: Add mixed case, numbers or symbols for luxury grade");
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_fair));
                break;
            case STRONG:
                passwordStrengthProgress.setProgress(100);
                passwordStrengthProgress.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.strength_strong)));
                tvPasswordStrength.setText("Strong: Luxury grade security");
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_strong));
                break;
        }
    }

    private void updatePasswordMatchGuidance() {
        if (tvPasswordMatch == null) return;

        String password = registerPassword.getText() != null ? registerPassword.getText().toString() : "";
        String confirm = registerConfirmPassword.getText() != null ? registerConfirmPassword.getText().toString() : "";

        if (confirm.isEmpty()) {
            tvPasswordMatch.setVisibility(View.GONE);
            return;
        }

        tvPasswordMatch.setVisibility(View.VISIBLE);
        if (password.equals(confirm)) {
            tvPasswordMatch.setText("✓ Passwords match");
            tvPasswordMatch.setTextColor(ContextCompat.getColor(this, R.color.strength_strong));
        } else {
            tvPasswordMatch.setText("✗ Passwords do not match");
            tvPasswordMatch.setTextColor(ContextCompat.getColor(this, R.color.strength_weak));
        }
    }

    private void performRegistration() {
        String name = registerName.getText() != null ? registerName.getText().toString().trim() : "";
        String email = registerEmail.getText() != null ? registerEmail.getText().toString().trim() : "";
        String password = registerPassword.getText() != null ? registerPassword.getText().toString() : "";
        String confirmPassword = registerConfirmPassword.getText() != null ? registerConfirmPassword.getText().toString() : "";

        boolean hasError = false;

        // Check if full name is empty
        if (name.isEmpty()) {
            if (registerNameLayout != null) {
                registerNameLayout.setError("Please enter your full name");
            }
            registerName.requestFocus();
            hasError = true;
        }

        // Check email
        if (email.isEmpty()) {
            if (registerEmailLayout != null) {
                registerEmailLayout.setError("Please enter your email address");
            }
            if (!hasError) registerEmail.requestFocus();
            hasError = true;
        } else if (!SecurityUtils.isValidEmail(email)) {
            if (registerEmailLayout != null) {
                registerEmailLayout.setError("Please enter a valid email address");
            }
            if (!hasError) registerEmail.requestFocus();
            hasError = true;
        }

        // Check password
        if (password.isEmpty()) {
            if (registerPasswordLayout != null) {
                registerPasswordLayout.setError("Please create a password");
            }
            if (!hasError) registerPassword.requestFocus();
            hasError = true;
        } else if (password.length() < 6) {
            if (registerPasswordLayout != null) {
                registerPasswordLayout.setError("Password must be at least 6 characters");
            }
            if (!hasError) registerPassword.requestFocus();
            hasError = true;
        }

        // Check confirm password
        if (confirmPassword.isEmpty()) {
            if (registerConfirmPasswordLayout != null) {
                registerConfirmPasswordLayout.setError("Please confirm your password");
            }
            if (!hasError) registerConfirmPassword.requestFocus();
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            if (registerConfirmPasswordLayout != null) {
                registerConfirmPasswordLayout.setError("Passwords do not match");
            }
            if (!hasError) registerConfirmPassword.requestFocus();
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(RegisterActivity.this, "Please complete all highlighted fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button & show progress to prevent duplicate submissions
        setLoadingState(true);

        // Add user to database
        boolean isInserted = dbHelper.addUser(name, email, password);
        if (isInserted) {
            Toast.makeText(RegisterActivity.this, "Registration Successful! Please sign in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            setLoadingState(false);
            if (registerEmailLayout != null) {
                registerEmailLayout.setError("This email is already registered");
            }
            Toast.makeText(RegisterActivity.this, "Email may already be registered. Try logging in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (registerButton != null) {
            registerButton.setEnabled(!isLoading);
            registerButton.setText(isLoading ? "" : "Create Account");
        }
        if (registerProgressBar != null) {
            registerProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
