package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.PaymentMethod;
import com.example.tech_store_mobile.R;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder> {

    private List<PaymentMethod> paymentList;
    private int selectedPosition = -1;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(PaymentMethod payment, int position);
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.listener = listener;
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentList) {
        this.paymentList = paymentList;
        updateSelectedPosition();
    }

    public void updateData(List<PaymentMethod> newList) {
        this.paymentList = newList;
        updateSelectedPosition();
        notifyDataSetChanged();
    }

    private void updateSelectedPosition() {
        if (paymentList == null) return;
        for (int i = 0; i < paymentList.size(); i++) {
            if (Boolean.TRUE.equals(paymentList.get(i).getIsDefault())) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentMethod payment = paymentList.get(position);
        
        // Hiển thị số thẻ đã được che (masking), chỉ giữ lại 4 số cuối
        String fullNumber = payment.getCardNumber();
        String maskedNumber = "";
        if (fullNumber != null && fullNumber.length() >= 4) {
            maskedNumber = "**** **** **** " + fullNumber.substring(fullNumber.length() - 4);
        } else {
            maskedNumber = fullNumber;
        }
        holder.tvCardNumber.setText(maskedNumber);
        
        holder.tvDefaultLabel.setVisibility(Boolean.TRUE.equals(payment.getIsDefault()) ? View.VISIBLE : View.GONE);
        holder.rbSelect.setChecked(position == selectedPosition);

        // Hiển thị logo dựa trên cardType
        if ("VISA".equalsIgnoreCase(payment.getCardType())) {
            holder.ivCardLogo.setImageResource(R.drawable.card); // Thay bằng ic_visa nếu có
        } else {
            holder.ivCardLogo.setImageResource(R.drawable.card); // Thay bằng ic_mastercard nếu có
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onPaymentClick(payment, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentList == null ? 0 : paymentList.size();
    }

    public PaymentMethod getSelectedPayment() {
        if (selectedPosition != -1 && selectedPosition < paymentList.size()) {
            return paymentList.get(selectedPosition);
        }
        return null;
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber, tvDefaultLabel;
        ImageView ivCardLogo;
        RadioButton rbSelect;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvDefaultLabel = itemView.findViewById(R.id.tv_default_payment_label);
            ivCardLogo = itemView.findViewById(R.id.iv_card_logo);
            rbSelect = itemView.findViewById(R.id.rb_select_payment);
        }
    }
}
