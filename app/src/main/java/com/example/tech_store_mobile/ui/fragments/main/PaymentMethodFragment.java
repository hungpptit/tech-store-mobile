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

import com.example.tech_store_mobile.Model.PaymentMethod;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.PaymentMethodAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentMethodFragment extends Fragment {
    private static final String TAG = "PaymentMethodFragment";

    private RecyclerView rvPaymentMethods;
    private PaymentMethodAdapter paymentAdapter;
    private List<PaymentMethod> paymentList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvPaymentMethods = view.findViewById(R.id.rv_payment_methods);
        paymentList = new ArrayList<>();
        
        setupRecyclerView();
        loadPaymentMethods();

        view.findViewById(R.id.btn_back_payment).setOnClickListener(v -> requireActivity().onBackPressed());

        // Xử lý khi bấm vào nút Add New Card
        view.findViewById(R.id.btn_add_new_card).setOnClickListener(v -> {
            replaceFragment(new AddCardFragment());
        });
        
        view.findViewById(R.id.btn_apply_payment).setOnClickListener(v -> {
            handleApplyPayment();
        });

        return view;
    }

    private void setupRecyclerView() {
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(getContext()));
        paymentAdapter = new PaymentMethodAdapter(paymentList);
        rvPaymentMethods.setAdapter(paymentAdapter);
    }

    private void loadPaymentMethods() {
        String userId = "user_001"; // Sử dụng ID tĩnh để test đồng bộ

        db.collection("payment_methods")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paymentList.clear();
                    paymentList.addAll(queryDocumentSnapshots.toObjects(PaymentMethod.class));
                    paymentAdapter.updateData(paymentList);
                    Log.d(TAG, "Loaded " + paymentList.size() + " cards");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading payment methods", e);
                    Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleApplyPayment() {
        PaymentMethod selected = paymentAdapter.getSelectedPayment();
        if (selected == null) {
            Toast.makeText(getContext(), "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Boolean.TRUE.equals(selected.getIsDefault())) {
            requireActivity().onBackPressed();
            return;
        }

        String userId = "user_001";
        
        db.collection("payment_methods")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "isDefault", false);
                    }

                    batch.update(db.collection("payment_methods").document(selected.getPaymentId()), "isDefault", true);

                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("defaultPaymentId", selected.getPaymentId());
                    batch.set(db.collection("users").document(userId), userUpdate, SetOptions.merge());

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Default payment updated", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
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
