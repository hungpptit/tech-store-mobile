package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private int selectedPosition = -1;
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onAddressClick(Address address, int position);
    }

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    public AddressAdapter(List<Address> addressList) {
        this.addressList = addressList;
        updateSelectedPosition();
    }

    public void updateData(List<Address> newList) {
        this.addressList = newList;
        updateSelectedPosition();
        notifyDataSetChanged();
    }

    private void updateSelectedPosition() {
        if (addressList == null) return;
        for (int i = 0; i < addressList.size(); i++) {
            if (Boolean.TRUE.equals(addressList.get(i).getIsDefault())) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvNickname.setText(address.getNickname());
        holder.tvFullAddress.setText(address.getFullAddress());
        
        holder.tvDefaultLabel.setVisibility(Boolean.TRUE.equals(address.getIsDefault()) ? View.VISIBLE : View.GONE);
        holder.rbSelect.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onAddressClick(address, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    public Address getSelectedAddress() {
        if (selectedPosition != -1 && selectedPosition < addressList.size()) {
            return addressList.get(selectedPosition);
        }
        return null;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvNickname, tvFullAddress, tvDefaultLabel;
        RadioButton rbSelect;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            tvDefaultLabel = itemView.findViewById(R.id.tv_default_label);
            rbSelect = itemView.findViewById(R.id.rb_select_address);
        }
    }
}
