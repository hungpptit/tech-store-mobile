package com.example.tech_store_mobile.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // Views Login
    private TextInputLayout tipLoginEmail, tipLoginPassword;
    private TextInputEditText edtLoginEmail, edtLoginPassword;
    private MaterialButton btnLogin;

    // Views Overlay Success
    private View layoutSuccessOverlay;
    private MaterialButton btnThanks;

    // Firebase & Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        configureGoogleSignIn();
        setupListeners();
        setupRealTimeValidation();
    }

    private void initViews() {
        tipLoginEmail = findViewById(R.id.tipLoginEmail);
        tipLoginPassword = findViewById(R.id.tipLoginPassword);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        layoutSuccessOverlay = findViewById(R.id.layoutSuccessOverlay);
        btnThanks = findViewById(R.id.btn_thanks);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.btnGoogleLogin).setOnClickListener(v -> startGoogleSignIn());

        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        findViewById(R.id.tvGoToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        if (btnThanks != null) {
            btnThanks.setOnClickListener(v -> {
                layoutSuccessOverlay.setVisibility(View.GONE);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupRealTimeValidation() {
        edtLoginEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // ĐỔI SANG MÀU XANH KHI ĐÚNG ĐỊNH DẠNG
                    tipLoginEmail.setBoxStrokeColor(Color.parseColor("#4CAF50"));
                    tipLoginEmail.setEndIconDrawable(R.drawable.check_circle);
                    tipLoginEmail.setEndIconTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                    tipLoginEmail.setError(null);
                } else {
                    tipLoginEmail.setBoxStrokeColor(Color.BLACK);
                    tipLoginEmail.setEndIconDrawable(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void attemptLogin() {
        String email = edtLoginEmail.getText().toString().trim();
        String password = edtLoginPassword.getText().toString();

        // 1. Reset lỗi cũ về null để bắt đầu kiểm tra mới
        tipLoginEmail.setError(null);
        tipLoginPassword.setError(null);

        boolean isValid = true; // Biến cờ đánh dấu trạng thái hợp lệ

        // 2. Kiểm tra Email (Check độc lập)
        if (email.isEmpty()) {
            tipLoginEmail.setError("Email address is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Nếu gõ linh tinh không đúng định dạng mail
            tipLoginEmail.setError("Invalid email format");
            isValid = false;
        }

        // 3. Kiểm tra Password (Check độc lập, KHÔNG dùng 'else if')
        if (password.isEmpty()) {
            tipLoginPassword.setError("Password is required");
            isValid = false;
        }

        // 4. Nếu có bất kỳ ô nào sai (isValid = false) thì dừng lại hiện lỗi, không gọi Firebase
        if (!isValid) {
            return;
        }

        // 5. Nếu gõ đúng định dạng hết rồi mới bắt đầu gọi Firebase để check tài khoản thật
        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Xử lý lỗi trả về từ Server Firebase
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "";

                        if (errorMessage.contains("password")) {
                            tipLoginPassword.setError("Invalid password. Please try again.");
                        } else if (errorMessage.contains("user") || errorMessage.contains("record")) {
                            tipLoginEmail.setError("Email not found. Please register first.");
                        } else {
                            tipLoginEmail.setError("Authentication failed.");
                        }
                    }
                });
    }

    private void showSuccessDialog() {
        if (layoutSuccessOverlay != null) {
            layoutSuccessOverlay.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    // --- PHẦN GOOGLE SIGN IN (GIỮ NGUYÊN) ---
    private void configureGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(result.getData());
                    }
                }
        );

        String webClientId = resolveDefaultWebClientId();
        if (webClientId != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
        }
    }

    private String resolveDefaultWebClientId() {
        int webClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        return webClientIdRes == 0 ? null : getString(webClientIdRes);
    }

    private void startGoogleSignIn() {
        if (googleSignInClient == null) return;
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    private void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(this, "Google Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) showSuccessDialog();
                });
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "Loading..." : "Login");
        btnLogin.setAlpha(isLoading ? 0.7f : 1.0f);
    }
}