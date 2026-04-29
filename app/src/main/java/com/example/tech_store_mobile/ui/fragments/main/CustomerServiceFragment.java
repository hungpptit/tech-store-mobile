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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.Model.ChatMessage;
import com.example.tech_store_mobile.Model.ChatRoom;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.SetOptions;

public class CustomerServiceFragment extends Fragment {

    private static final String TAG = "CustomerServiceChat";
    private EditText edtChatMessage;
    private ImageButton btnAction;
    private LinearLayout chatMessagesContainer;

    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserName = "Customer";
    private ListenerRegistration messagesListener;

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

        db = FirebaseFirestore.getInstance();
        currentUserId = AuthManager.getCurrentUid();

        if (currentUserId != null) {
            fetchUserProfile();
            listenForMessages();
        }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }

    private void fetchUserProfile() {
        if (currentUserId == null) return;
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        if (name != null) currentUserName = name;
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user profile", e));
    }

    private void listenForMessages() {
        if (currentUserId == null) return;

        messagesListener = db.collection("rooms")
                .document(currentUserId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        chatMessagesContainer.removeAllViews();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) {
                                boolean isMe = msg.getSenderId().equals(currentUserId);
                                appendMessageBubble(msg.getContent(), isMe, msg.getCreatedAt());
                            }
                        }
                    }
                });

        db.collection("rooms").document(currentUserId)
                .update("userUnreadCount", 0)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to reset unread count", e));
    }

    private void handleActionClick() {
        if (edtChatMessage == null || btnAction == null || currentUserId == null) {
            return;
        }

        String messageContent = edtChatMessage.getText() != null ? edtChatMessage.getText().toString().trim() : "";
        if (messageContent.isEmpty()) {
            showComingSoon();
            return;
        }

        sendMessage(messageContent);
        edtChatMessage.setText("");
        updateActionButtonState("");
    }

    private void sendMessage(String content) {
        if (currentUserId == null) return;

        WriteBatch batch = db.batch();

        DocumentReference messageRef = db.collection("rooms")
                .document(currentUserId)
                .collection("messages")
                .document();

        ChatMessage message = new ChatMessage(
                currentUserId,
                "admin",
                content,
                "text",
                null,
                Timestamp.now()
        );
        batch.set(messageRef, message);

        DocumentReference roomRef = db.collection("rooms").document(currentUserId);
        
        batch.set(roomRef, new ChatRoom(
                currentUserId,
                currentUserName,
                content,
                Timestamp.now(),
                0L,
                1L
        ), SetOptions.merge());

        batch.update(roomRef, 
            "lastMessage", content,
            "updatedAt", FieldValue.serverTimestamp(),
            "adminUnreadCount", FieldValue.increment(1)
        );

        batch.commit().addOnFailureListener(e -> {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
        });
    }

    private void appendMessageBubble(String message, boolean isMe, Timestamp timestamp) {
        if (chatMessagesContainer == null || getContext() == null) {
            return;
        }

        TextView bubble = new TextView(requireContext());
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.gravity = isMe ? Gravity.END : Gravity.START;
        bubbleParams.topMargin = dpToPx(16);
        bubble.setLayoutParams(bubbleParams);
        
        if (isMe) {
            bubble.setBackgroundResource(R.drawable.bg_button_black);
            bubble.setTextColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
        } else {
            bubble.setBackgroundResource(R.drawable.bg_search_bar);
            bubble.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        
        bubble.setText(message);
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        bubble.setPadding(dpToPx(18), dpToPx(10), dpToPx(18), dpToPx(10));
        bubble.setMaxWidth(dpToPx(MAX_BUBBLE_WIDTH_DP));

        TextView time = new TextView(requireContext());
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.gravity = isMe ? Gravity.END : Gravity.START;
        timeParams.topMargin = dpToPx(6);
        time.setLayoutParams(timeParams);
        
        String timeStr = "Now";
        if (timestamp != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            timeStr = sdf.format(timestamp.toDate());
        }
        time.setText(timeStr);
        time.setTextColor(getResources().getColor(android.R.color.darker_gray, requireContext().getTheme()));
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        chatMessagesContainer.addView(bubble);
        chatMessagesContainer.addView(time);

        scrollToBottom();
    }

    private void scrollToBottom() {
        View scrollView = requireView().findViewById(R.id.chatScroll);
        if (scrollView instanceof android.widget.ScrollView) {
            ((android.widget.ScrollView) scrollView).post(() -> 
                ((android.widget.ScrollView) scrollView).fullScroll(View.FOCUS_DOWN));
        } else if (scrollView instanceof androidx.core.widget.NestedScrollView) {
            ((androidx.core.widget.NestedScrollView) scrollView).post(() -> 
                ((androidx.core.widget.NestedScrollView) scrollView).fullScroll(View.FOCUS_DOWN));
        }
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
        appendMessageBubble(message, true, Timestamp.now());
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

