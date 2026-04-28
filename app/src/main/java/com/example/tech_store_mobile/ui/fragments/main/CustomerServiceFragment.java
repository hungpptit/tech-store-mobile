package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.R;

public class CustomerServiceFragment extends Fragment {

    private EditText edtChatMessage;
    private ImageButton btnAction;
    private LinearLayout chatMessagesContainer;

    private static final int MAX_BUBBLE_WIDTH_DP = 260;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_service_chat, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        view.findViewById(R.id.btnCall).setOnClickListener(v -> showComingSoon());

        edtChatMessage = view.findViewById(R.id.edtChatMessage);
        btnAction = view.findViewById(R.id.btnSendVoice);
        chatMessagesContainer = view.findViewById(R.id.chatMessagesContainer);

        if (btnAction != null) {
            updateActionButtonState(edtChatMessage != null ? edtChatMessage.getText().toString() : "");
            btnAction.setOnClickListener(v -> handleActionClick());
        }

        if (edtChatMessage != null) {
            edtChatMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateActionButtonState(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        return view;
    }

    private void handleActionClick() {
        if (edtChatMessage == null || btnAction == null) {
            return;
        }

        String message = edtChatMessage.getText() != null ? edtChatMessage.getText().toString().trim() : "";
        if (message.isEmpty()) {
            showComingSoon();
            return;
        }

        appendFakeMessage(message);
        edtChatMessage.setText("");
        updateActionButtonState("");
    }

    private void updateActionButtonState(String message) {
        if (btnAction == null || getContext() == null) {
            return;
        }

        boolean hasText = message != null && !message.trim().isEmpty();
        btnAction.setImageResource(hasText ? R.drawable.ic_send : R.drawable.ic_mic);
        btnAction.setContentDescription(getString(hasText
                ? R.string.customer_service_send_content_description
                : R.string.customer_service_mic_content_description));
    }

    private void appendFakeMessage(String message) {
        if (chatMessagesContainer == null || getContext() == null) {
            return;
        }

        TextView bubble = new TextView(requireContext());
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.gravity = Gravity.END;
        bubbleParams.topMargin = dpToPx(16);
        bubble.setLayoutParams(bubbleParams);
        bubble.setBackgroundResource(R.drawable.bg_button_black);
        bubble.setText(message);
        bubble.setTextColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        bubble.setPadding(dpToPx(18), dpToPx(10), dpToPx(18), dpToPx(10));
        bubble.setMaxWidth(dpToPx(MAX_BUBBLE_WIDTH_DP));

        TextView time = new TextView(requireContext());
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.gravity = Gravity.END;
        timeParams.topMargin = dpToPx(6);
        time.setLayoutParams(timeParams);
        time.setText(R.string.customer_service_time_now);
        time.setTextColor(getResources().getColor(android.R.color.darker_gray, requireContext().getTheme()));
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        int insertIndex = Math.max(chatMessagesContainer.getChildCount() - 1, 0);
        chatMessagesContainer.addView(bubble, insertIndex);
        chatMessagesContainer.addView(time, insertIndex + 1);

        View scrollView = requireView().findViewById(R.id.chatScroll);
        if (scrollView instanceof android.widget.ScrollView) {
            ((android.widget.ScrollView) scrollView).fullScroll(View.FOCUS_DOWN);
        } else if (scrollView instanceof androidx.core.widget.NestedScrollView) {
            ((androidx.core.widget.NestedScrollView) scrollView).post(() -> ((androidx.core.widget.NestedScrollView) scrollView).fullScroll(View.FOCUS_DOWN));
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private void showComingSoon() {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(requireContext(), R.string.auth_feature_coming_soon, Toast.LENGTH_SHORT).show();
    }
}

