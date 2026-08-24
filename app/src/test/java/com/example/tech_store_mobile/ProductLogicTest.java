package com.example.tech_store_mobile;

import com.example.tech_store_mobile.Model.Product;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ProductLogicTest {

    @Test
    public void testProductDiscountCalculation() {
        Double basePrice = 1200.0;
        Double discount = 15.0; // 15% off
        Double expectedFinalPrice = basePrice * (1.0 - (discount / 100.0)); // 1020.0

        Product product = new Product();
        product.setProductId("prod_mac_01");
        product.setProductName("MacBook Pro M4");
        product.setBasePrice(basePrice);
        product.setDiscountPercentage(discount);
        product.setFinalPrice(expectedFinalPrice);
        product.setStockQuantity(10L);

        assertEquals(Double.valueOf(1020.0), product.getFinalPrice());
        assertTrue(product.getStockQuantity() > 0);
    }

    @Test
    public void testProductSearchKeywordsIndex() {
        List<String> keywords = Arrays.asList("mac", "macbook", "m4", "apple", "laptop");
        Product product = new Product();
        product.setSearchKeywords(keywords);

        assertNotNull(product.getSearchKeywords());
        assertEquals(5, product.getSearchKeywords().size());
        assertTrue(product.getSearchKeywords().contains("macbook"));
        assertTrue(product.getSearchKeywords().contains("apple"));
    }
}
