package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.UserNotification;
import com.example.tech_store_mobile.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<UserNotification> notifications;
    private OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(UserNotification notification, int position);
    }

    public NotificationAdapter(List<UserNotification> notifications) {
        this.notifications = notifications;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        UserNotification notification = notifications.get(position);
        holder.bind(notification, position);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvTime;
        private final View ivReadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivReadIndicator = itemView.findViewById(R.id.view_read_indicator);
        }

        public void bind(UserNotification notification, int position) {
            tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "");
            
            String content = notification.getContent() != null ? notification.getContent() : "";
            tvContent.setText(com.example.tech_store_mobile.utils.OrderStatusUtil.translateNotificationContent(content));

            // Format time
            if (notification.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                tvTime.setText(sdf.format(notification.getCreatedAt().toDate()));
            }

            // Show read indicator (blue dot if unread)
            ivReadIndicator.setVisibility(
                    notification.getIsRead() != null && notification.getIsRead()
                            ? View.GONE
                            : View.VISIBLE
            );

            // Click to mark as read
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification, getBindingAdapterPosition());
                }
            });
        }
    }
}


