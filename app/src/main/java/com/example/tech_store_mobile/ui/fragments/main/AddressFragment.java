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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.AddressAdapter;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressFragment extends Fragment {
    private static final String TAG = "AddressFragment";

    private RecyclerView rvAddresses;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        db = FirebaseFirestore.getInstance();

        rvAddresses = view.findViewById(R.id.rv_addresses);
        addressList = new ArrayList<>();
        
        setupRecyclerView();
        loadAddresses();

        view.findViewById(R.id.btn_back_address).setOnClickListener(v -> requireActivity().onBackPressed());

        // Xử lý khi bấm vào nút Add New Address
        view.findViewById(R.id.btn_add_new_address).setOnClickListener(v -> {
            replaceFragment(new AddAddressFragment());
        });
        
        view.findViewById(R.id.btn_apply_address).setOnClickListener(v -> {
            handleApplyAddress();
        });

        return view;
    }

    private void setupRecyclerView() {
        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        addressAdapter = new AddressAdapter(addressList);
        rvAddresses.setAdapter(addressAdapter);
    }

    private void loadAddresses() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    addressList.addAll(queryDocumentSnapshots.toObjects(Address.class));
                    addressAdapter.updateData(addressList);
                    Log.d(TAG, "Loaded " + addressList.size() + " addresses");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading addresses", e);
                    Toast.makeText(getContext(), "Failed to load addresses", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleApplyAddress() {
        Address selectedAddress = addressAdapter.getSelectedAddress();
        if (selectedAddress == null) {
            Toast.makeText(getContext(), "Please select an address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu đã là mặc định thì không cần cập nhật DB
        if (Boolean.TRUE.equals(selectedAddress.getIsDefault())) {
            requireActivity().onBackPressed();
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    // 1. Tắt mặc định cũ
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "isDefault", false);
                    }

                    // 2. Bật mặc định mới
                    batch.update(db.collection("addresses").document(selectedAddress.getAddressId()), "isDefault", true);

                    // 3. Cập nhật User Profile
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("defaultAddressId", selectedAddress.getAddressId());
                    batch.set(db.collection("users").document(userId), userUpdate, SetOptions.merge());

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Default address updated to: " + selectedAddress.getNickname(), Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
