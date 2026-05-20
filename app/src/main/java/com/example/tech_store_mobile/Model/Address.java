package com.example.tech_store_mobile.Model;

@SuppressWarnings("unused")
public class Address {
    private String addressId;
    private String userId;
    private String nickname;
    private AddressLocation location;
    private String fullAddress;
    private Boolean isDefault;

    public Address() {
    }

    public Address(String addressId, String userId, String nickname, String fullAddress, Boolean isDefault) {
        this(addressId, userId, nickname, null, fullAddress, isDefault);
    }

    public Address(String addressId, String userId, String nickname, AddressLocation location, String fullAddress, Boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.nickname = nickname;
        this.location = location;
        this.fullAddress = resolveFullAddress(location, fullAddress);
        this.isDefault = isDefault;
    }

    public String getAddressId() {
        return addressId;
    }

    @SuppressWarnings("unused")
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    @SuppressWarnings("unused")
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public AddressLocation getLocation() {
        return location;
    }

    public void setLocation(AddressLocation location) {
        this.location = location;
        if (this.fullAddress == null || this.fullAddress.trim().isEmpty()) {
            this.fullAddress = resolveFullAddress(location, null);
        } else if (location != null && (location.getFullAddress() == null || location.getFullAddress().trim().isEmpty())) {
            location.setFullAddress(this.fullAddress);
        }
    }

    public String getFullAddress() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            return fullAddress;
        }
        return resolveFullAddress(location, null);
    }

    @SuppressWarnings("unused")
    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
        if (this.location != null) {
            this.location.setFullAddress(fullAddress);
        }
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    @SuppressWarnings("unused")
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    private static String resolveFullAddress(AddressLocation location, String fallback) {
        if (location == null) {
            return fallback;
        }

        if (location.getFullAddress() != null && !location.getFullAddress().trim().isEmpty()) {
            return location.getFullAddress();
        }

        StringBuilder builder = new StringBuilder();
        appendPart(builder, location.getDetail());
        appendPart(builder, location.getWardName());
        appendPart(builder, location.getDistrictName());
        appendPart(builder, location.getProvinceName());

        String resolved = builder.toString();
        if (!resolved.trim().isEmpty()) {
            return resolved;
        }

        return fallback;
    }

    private static void appendPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    public static class AddressLocation {
        private String provinceCode;
        private String provinceName;
        private String districtCode;
        private String districtName;
        private String wardCode;
        private String wardName;
        private String detail;
        private String fullAddress;

        public AddressLocation() {
        }

        public AddressLocation(String provinceCode, String provinceName, String districtCode, String districtName,
                               String wardCode, String wardName, String detail, String fullAddress) {
            this.provinceCode = provinceCode;
            this.provinceName = provinceName;
            this.districtCode = districtCode;
            this.districtName = districtName;
            this.wardCode = wardCode;
            this.wardName = wardName;
            this.detail = detail;
            this.fullAddress = fullAddress;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }

        public String getDistrictCode() {
            return districtCode;
        }

        public void setDistrictCode(String districtCode) {
            this.districtCode = districtCode;
        }

        public String getDistrictName() {
            return districtName;
        }

        public void setDistrictName(String districtName) {
            this.districtName = districtName;
        }

        public String getWardCode() {
            return wardCode;
        }

        public void setWardCode(String wardCode) {
            this.wardCode = wardCode;
        }

        public String getWardName() {
            return wardName;
        }

        public void setWardName(String wardName) {
            this.wardName = wardName;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getFullAddress() {
            if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                return fullAddress;
            }

            StringBuilder builder = new StringBuilder();
            Address.appendPart(builder, detail);
            Address.appendPart(builder, wardName);
            Address.appendPart(builder, districtName);
            Address.appendPart(builder, provinceName);
            return builder.toString();
        }

        public void setFullAddress(String fullAddress) {
            this.fullAddress = fullAddress;
        }
    }
}

