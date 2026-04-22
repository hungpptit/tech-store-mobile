package com.example.tech_store_mobile.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tech_store_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

	private TextInputEditText edtForgotEmail;
	private MaterialButton btnResetPassword;
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		mAuth = FirebaseAuth.getInstance();
		bindViews();
		setupListeners();
	}

	private void bindViews() {
		edtForgotEmail = findViewById(R.id.edtForgotEmail);
		btnResetPassword = findViewById(R.id.btnResetPassword);
	}

	private void setupListeners() {
		if (btnResetPassword != null) {
			btnResetPassword.setOnClickListener(v -> sendResetRequest());
		}

		ImageView btnForgotBack = findViewById(R.id.btnForgotBack);
		if (btnForgotBack != null) {
			btnForgotBack.setOnClickListener(v -> finish());
		}

		TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);
		if (tvBackToLogin != null) {
			tvBackToLogin.setOnClickListener(v -> finish());
		}
	}

	private void sendResetRequest() {
		if (edtForgotEmail == null || btnResetPassword == null) {
			return;
		}

		String email = edtForgotEmail.getText() != null ? edtForgotEmail.getText().toString().trim() : "";
		if (email.isEmpty()) {
			edtForgotEmail.setError(getString(R.string.auth_error_email_required));
			edtForgotEmail.requestFocus();
			return;
		}

		if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			edtForgotEmail.setError(getString(R.string.auth_error_email_invalid));
			edtForgotEmail.requestFocus();
			return;
		}

		setLoading(true);
		mAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener(task -> {
					setLoading(false);
					if (task.isSuccessful()) {
						Toast.makeText(this, R.string.auth_reset_email_sent, Toast.LENGTH_LONG).show();
						finish();
						return;
					}

					String errorMessage = task.getException() != null
							? task.getException().getLocalizedMessage()
							: getString(R.string.auth_reset_email_failed);
					Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
				});
	}

	private void setLoading(boolean isLoading) {
		if (btnResetPassword == null) {
			return;
		}
		btnResetPassword.setEnabled(!isLoading);
		btnResetPassword.setText(isLoading ? R.string.auth_loading : R.string.auth_send_request_button);
		btnResetPassword.setAlpha(isLoading ? 0.7f : 1f);
	}
}
