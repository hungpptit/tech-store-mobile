package com.example.tech_store_mobile.ui.fragments.main;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.VietnamProvincesApiClient;
import com.example.tech_store_mobile.utils.VietnamProvincesApiClient.DistrictOption;
import com.example.tech_store_mobile.utils.VietnamProvincesApiClient.ProvinceDetail;
import com.example.tech_store_mobile.utils.VietnamProvincesApiClient.ProvinceOption;
import com.example.tech_store_mobile.utils.VietnamProvincesApiClient.WardOption;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddAddressFragment extends Fragment {

    private Spinner spinnerNickname;
    private Spinner spinnerProvince;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private EditText etAddressDetail;
    private TextView tvFullAddressPreview;
    private CheckBox cbDefault;
    private AppCompatButton btnAdd;
    private FirebaseFirestore db;
    private final VietnamProvincesApiClient provincesApiClient = new VietnamProvincesApiClient();

    private final List<ProvinceOption> provinceOptions = new ArrayList<>();
    private ProvinceDetail currentProvinceDetail;
    private ProvinceOption selectedProvince;
    private DistrictOption selectedDistrict;
    private WardOption selectedWard;
    private String pendingProvinceCode;
    private List<WardOption> currentWards = new ArrayList<>();  // Keep loaded wards separately

    private ArrayAdapter<String> provinceAdapter;
    private ArrayAdapter<String> districtAdapter;
    private ArrayAdapter<String> wardAdapter;

    private final String[] nicknames = {"Home", "Office", "Apartment", "Parent's House"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_address, container, false);

        db = FirebaseFirestore.getInstance();

        spinnerNickname = view.findViewById(R.id.spinner_nickname);
        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        etAddressDetail = view.findViewById(R.id.et_address_detail);
        tvFullAddressPreview = view.findViewById(R.id.tv_full_address_preview);
        cbDefault = view.findViewById(R.id.cb_default_address);
        btnAdd = view.findViewById(R.id.btn_add_address_submit);

        setupSpinner();
        setupLocationSpinners();
        setupTextWatcher();
        loadProvinces();

        view.findViewById(R.id.btn_back_add_address).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        btnAdd.setOnClickListener(v -> saveAddress());

        return view;
    }

    private void loadDistrictWards(String districtCode) {
        clearWards();
        provincesApiClient.fetchDistrictWards(districtCode, new VietnamProvincesApiClient.Callback<>() {
            @Override
            public void onSuccess(List<VietnamProvincesApiClient.WardOption> data) {
                if (!isAdded()) {
                    return;
                }

                Log.d("AddAddressFragment", "Loaded " + data.size() + " wards");
                populateWards(data);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), TextUtils.isEmpty(message) ? "Failed to load wards" : message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nicknames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNickname.setAdapter(adapter);
    }

    private void setupLocationSpinners() {
        provinceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        districtAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        wardAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);

        spinnerProvince.setEnabled(false);
        spinnerDistrict.setEnabled(false);
        spinnerWard.setEnabled(false);

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > provinceOptions.size()) {
                    selectedProvince = null;
                    currentProvinceDetail = null;
                    selectedDistrict = null;
                    selectedWard = null;
                    clearDistricts();
                    clearWards();
                    updatePreview();
                    updateSubmitState();
                    return;
                }

                selectedProvince = provinceOptions.get(position - 1);
                selectedDistrict = null;
                selectedWard = null;
                clearDistricts();
                clearWards();
                updatePreview();
                updateSubmitState();
                loadProvinceDetail(selectedProvince.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvince = null;
                currentProvinceDetail = null;
                selectedDistrict = null;
                selectedWard = null;
                clearDistricts();
                clearWards();
                updatePreview();
                updateSubmitState();
            }
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentProvinceDetail == null || position <= 0 || position > currentProvinceDetail.getDistricts().size()) {
                    selectedDistrict = null;
                    selectedWard = null;
                    clearWards();
                    updatePreview();
                    updateSubmitState();
                    return;
                }

                selectedDistrict = currentProvinceDetail.getDistricts().get(position - 1);
                selectedWard = null;
                Log.d("AddAddressFragment", "Selected district: " + selectedDistrict.getName());
                // Call API to fetch wards for this district (API at /d/{code}?depth=2 returns populated wards)
                loadDistrictWards(selectedDistrict.getCode());
                updatePreview();
                updateSubmitState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
                selectedWard = null;
                clearWards();
                updatePreview();
                updateSubmitState();
            }
        });

        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > currentWards.size()) {
                    selectedWard = null;
                } else {
                    selectedWard = currentWards.get(position - 1);
                }
                updatePreview();
                updateSubmitState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWard = null;
                updatePreview();
                updateSubmitState();
            }
        });
    }

    private void setupTextWatcher() {
        etAddressDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
                updateSubmitState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btnAdd.setEnabled(false);
        btnAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
        updatePreview();
    }

    private void loadProvinces() {
        provincesApiClient.fetchProvinces(new VietnamProvincesApiClient.Callback<>() {
            @Override
            public void onSuccess(List<ProvinceOption> data) {
                if (!isAdded()) {
                    return;
                }

                provinceOptions.clear();
                if (data != null) {
                    provinceOptions.addAll(data);
                }

                List<String> labels = new ArrayList<>();
                labels.add("Select province/city");
                for (ProvinceOption province : provinceOptions) {
                    labels.add(formatItem(province.getName(), province.getCode()));
                }

                provinceAdapter.clear();
                provinceAdapter.addAll(labels);
                provinceAdapter.notifyDataSetChanged();
                spinnerProvince.setEnabled(true);
                spinnerProvince.setSelection(0, false);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), TextUtils.isEmpty(message) ? "Failed to load provinces" : message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProvinceDetail(String provinceCode) {
        pendingProvinceCode = provinceCode;
        clearDistricts();
        clearWards();

        provincesApiClient.fetchProvinceDetail(provinceCode, new VietnamProvincesApiClient.Callback<>() {
            @Override
            public void onSuccess(ProvinceDetail data) {
                if (!isAdded() || data == null || !provinceCode.equals(pendingProvinceCode)) {
                    return;
                }

                Log.d("AddAddressFragment", "Loaded province: " + data.getName() + " with " + data.getDistricts().size() + " districts");
                currentProvinceDetail = data;
                List<String> districtLabels = new ArrayList<>();
                districtLabels.add("Select district");
                for (DistrictOption district : data.getDistricts()) {
                    Log.d("AddAddressFragment", "  District: " + district.getName() + " has " + district.getWards().size() + " wards");
                    districtLabels.add(formatItem(district.getName(), district.getCode()));
                }

                districtAdapter.clear();
                districtAdapter.addAll(districtLabels);
                districtAdapter.notifyDataSetChanged();
                spinnerDistrict.setEnabled(true);
                spinnerDistrict.setSelection(0, false);
                updatePreview();
                updateSubmitState();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || !provinceCode.equals(pendingProvinceCode)) {
                    return;
                }

                currentProvinceDetail = null;
                Toast.makeText(requireContext(), TextUtils.isEmpty(message) ? "Failed to load district data" : message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateWards(List<WardOption> wards) {
        currentWards.clear();
        if (wards != null) {
            currentWards.addAll(wards);
        }

        List<String> wardLabels = new ArrayList<>();
        wardLabels.add("Select ward/commune");
        for (WardOption ward : currentWards) {
            wardLabels.add(formatItem(ward.getName(), ward.getCode()));
        }

        wardAdapter.clear();
        wardAdapter.addAll(wardLabels);
        wardAdapter.notifyDataSetChanged();
        spinnerWard.setEnabled(true);
        spinnerWard.setSelection(0, false);
    }

    private void clearDistricts() {
        districtAdapter.clear();
        districtAdapter.add("Select district");
        districtAdapter.notifyDataSetChanged();
        spinnerDistrict.setEnabled(false);
    }

    private void clearWards() {
        currentWards.clear();
        wardAdapter.clear();
        wardAdapter.add("Select ward/commune");
        wardAdapter.notifyDataSetChanged();
        spinnerWard.setEnabled(false);
    }

    private void updatePreview() {
        if (tvFullAddressPreview != null) {
            tvFullAddressPreview.setText(buildFullAddressPreview());
        }
    }

    private void updateSubmitState() {
        boolean isValid = selectedProvince != null
                && selectedDistrict != null
                && selectedWard != null
                && !TextUtils.isEmpty(getAddressDetail());
        btnAdd.setEnabled(isValid);
        btnAdd.setBackgroundTintList(ColorStateList.valueOf(isValid ? Color.BLACK : Color.parseColor("#CCCCCC")));
    }

    private void saveAddress() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProvince == null || selectedDistrict == null || selectedWard == null) {
            Toast.makeText(getContext(), "Vui lòng chọn đầy đủ Tỉnh/Quận/Phường.", Toast.LENGTH_SHORT).show();
            return;
        }

        String detail = getAddressDetail();
        if (TextUtils.isEmpty(detail)) {
            Toast.makeText(getContext(), "Vui lòng nhập địa chỉ chi tiết.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdd.setEnabled(false);

        String nickname = spinnerNickname.getSelectedItem().toString();
        String fullAddress = buildFullAddressPreview();
        boolean isDefault = cbDefault.isChecked();
        String addressId = UUID.randomUUID().toString();

        Address.AddressLocation location = new Address.AddressLocation(
                selectedProvince.getCode(),
                selectedProvince.getName(),
                selectedDistrict.getCode(),
                selectedDistrict.getName(),
                selectedWard.getCode(),
                selectedWard.getName(),
                detail,
                fullAddress
        );

        Address newAddress = new Address(addressId, userId, nickname, location, fullAddress, isDefault);

        if (isDefault) {
            db.collection("addresses")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isDefault", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        WriteBatch batch = db.batch();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            batch.update(doc.getReference(), "isDefault", false);
                        }

                        batch.set(db.collection("addresses").document(addressId), newAddress);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("defaultAddressId", addressId);
                        batch.set(db.collection("users").document(userId), userData, SetOptions.merge());

                        batch.commit()
                                .addOnSuccessListener(aVoid -> showSuccessDialog())
                                .addOnFailureListener(e -> {
                                    btnAdd.setEnabled(true);
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnAdd.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            db.collection("addresses").document(addressId).set(newAddress)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> {
                        btnAdd.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private String getAddressDetail() {
        return etAddressDetail == null ? "" : etAddressDetail.getText().toString().trim();
    }

    private String buildFullAddressPreview() {
        String detail = getAddressDetail();
        String wardName = selectedWard != null ? selectedWard.getName() : "";
        String districtName = selectedDistrict != null ? selectedDistrict.getName() : "";
        String provinceName = selectedProvince != null ? selectedProvince.getName() : "";

        StringBuilder builder = new StringBuilder();
        appendPart(builder, detail);
        appendPart(builder, wardName);
        appendPart(builder, districtName);
        appendPart(builder, provinceName);

        String preview = builder.toString();
        if (TextUtils.isEmpty(preview)) {
            return "Full address preview will appear here";
        }
        return preview;
    }

    private void appendPart(StringBuilder builder, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private String formatItem(String name, String code) {
        if (TextUtils.isEmpty(code)) {
            return name;
        }
        if (TextUtils.isEmpty(name)) {
            return code;
        }
        return name + " (" + code + ")";
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);

        TextView title = dialog.findViewById(R.id.tv_success_title);
        TextView message = dialog.findViewById(R.id.tv_success_message);
        if (title != null) title.setText(R.string.dialog_address_success_title);
        if (message != null) message.setText(R.string.dialog_address_success_message);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.setCancelable(false);

        AppCompatButton btnThanks = dialog.findViewById(R.id.btn_thanks);
        if (btnThanks != null) {
            btnThanks.setText(R.string.dialog_address_success_button);
            btnThanks.setOnClickListener(v -> {
                dialog.dismiss();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }

        dialog.show();
    }
}
