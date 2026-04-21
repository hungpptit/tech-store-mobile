package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.AddressAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AddressFragment extends Fragment {
    private static final String TAG = "AddressFragment";

    private RecyclerView rvAddresses;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvAddresses = view.findViewById(R.id.rv_addresses);
        addressList = new ArrayList<>();
        
        setupRecyclerView();
        loadAddresses();

        view.findViewById(R.id.btn_back_address).setOnClickListener(v -> requireActivity().onBackPressed());
        
        view.findViewById(R.id.btn_apply_address).setOnClickListener(v -> {
            Address selected = addressAdapter.getSelectedAddress();
            if (selected != null) {
                // Xử lý khi chọn địa chỉ và nhấn Apply (ví dụ: quay về màn hình Checkout)
                Toast.makeText(getContext(), "Selected: " + selected.getNickname(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please select an address", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        addressAdapter = new AddressAdapter(addressList);
        rvAddresses.setAdapter(addressAdapter);
    }

    private void loadAddresses() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    addressList.addAll(queryDocumentSnapshots.toObjects(Address.class));
                    addressAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + addressList.size() + " addresses");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading addresses", e);
                    Toast.makeText(getContext(), "Failed to load addresses", Toast.LENGTH_SHORT).show();
                });
    }
}
