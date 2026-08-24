package com.example.tech_store_mobile;

import com.example.tech_store_mobile.Model.Address;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddressModelTest {

    @Test
    public void testAddressFullAddressResolutionFromLocation() {
        Address.AddressLocation location = new Address.AddressLocation(
                "79", "Thành phố Hồ Chí Minh",
                "760", "Quận 1",
                "26734", "Phường Bến Nghé",
                "97 Man Thiện", null
        );

        Address address = new Address("addr_01", "usr_100", "Home", location, null, true);

        assertEquals("addr_01", address.getAddressId());
        assertEquals("Home", address.getNickname());
        assertTrue(address.getIsDefault());
        assertEquals("97 Man Thiện, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh", address.getFullAddress());
    }

    @Test
    public void testAddressFallbackAddress() {
        Address address = new Address("addr_02", "usr_100", "Office", "123 Le Loi Street", false);
        assertEquals("123 Le Loi Street", address.getFullAddress());
        assertFalse(address.getIsDefault());
    }
}
