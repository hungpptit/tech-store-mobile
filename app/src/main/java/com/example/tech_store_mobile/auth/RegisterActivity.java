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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tipRegisterName, tipRegisterEmail, tipRegisterPassword;
    private TextInputEditText edtRegisterName, edtRegisterEmail, edtRegisterPassword;
    private MaterialButton btnSignUp;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn();
        bindViews();
        setupListeners();
        setupRealTimeValidation(); // Thêm lắng nghe gõ phím để hiện tick xanh
    }

    private void bindViews() {
        tipRegisterName = findViewById(R.id.tipRegisterName);
        tipRegisterEmail = findViewById(R.id.tipRegisterEmail);
        tipRegisterPassword = findViewById(R.id.tipRegisterPassword);

        edtRegisterName = findViewById(R.id.edtRegisterName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
    }

    private void setupListeners() {
        if (btnSignUp != null) {
            btnSignUp.setOnClickListener(v -> attemptSignUp());
        }

        View btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        if (btnGoogleRegister != null) {
            btnGoogleRegister.setOnClickListener(v -> startGoogleSignIn());
        }

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> finish());
        }
    }

    // Logic hiện dấu tick xanh khi gõ đúng định dạng Email
    private void setupRealTimeValidation() {
        // 1. Xử lý cho ô Username (Hết trống là hết lỗi)
        edtRegisterName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tipRegisterName.setError(null); // Xóa lỗi đỏ ngay khi bắt đầu gõ
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. Xử lý cho ô Email (Đã có - mình bổ sung thêm phần xóa lỗi khi gõ)
        edtRegisterEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                tipRegisterEmail.setError(null); // Cứ gõ là xóa lỗi đỏ cũ

                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tipRegisterEmail.setBoxStrokeColor(Color.parseColor("#4CAF50"));
                    tipRegisterEmail.setEndIconDrawable(R.drawable.check_circle);
                    tipRegisterEmail.setEndIconTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                } else {
                    tipRegisterEmail.setBoxStrokeColor(Color.BLACK);
                    tipRegisterEmail.setEndIconDrawable(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 3. Xử lý cho ô Password (Hết trống là hết lỗi)
        edtRegisterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tipRegisterPassword.setError(null); // Xóa lỗi đỏ ngay khi gõ mật khẩu
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void attemptSignUp() {
        String username = edtRegisterName.getText().toString().trim();
        String email = edtRegisterEmail.getText().toString().trim();
        String password = edtRegisterPassword.getText().toString();

        // 1. Reset lỗi cũ
        tipRegisterName.setError(null);
        tipRegisterEmail.setError(null);
        tipRegisterPassword.setError(null);

        boolean isValid = true;

        // 2. Check Name
        if (username.isEmpty()) {
            tipRegisterName.setError("Username is required");
            isValid = false;
        }

        // 3. Check Email
        if (email.isEmpty()) {
            tipRegisterEmail.setError("Email address is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tipRegisterEmail.setError("Invalid email format");
            isValid = false;
        }

        // 4. Check Password
        if (password.isEmpty()) {
            tipRegisterPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tipRegisterPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) return;

        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // 5. Bắt lỗi trùng Email từ Firebase
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            tipRegisterEmail.setError("This email address is already in use.");
                            edtRegisterEmail.requestFocus();
                        } else {
                            String errorMsg = exception != null ? exception.getMessage() : "Registration failed";
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (btnSignUp == null) return;
        btnSignUp.setEnabled(!isLoading);
        btnSignUp.setText(isLoading ? "Loading..." : "Create Account");
        btnSignUp.setAlpha(isLoading ? 0.7f : 1f);
    }
}