package com.example.tech_store_mobile;

import com.example.tech_store_mobile.Model.OrderSummary;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderSummaryTest {

    @Test
    public void testOrderSummaryCalculation_withValidValues() {
        Double subTotal = 250.00;
        Double shippingFee = 15.00;
        Double vatAmount = 25.00;
        Double totalAmount = subTotal + shippingFee + vatAmount; // 290.00

        OrderSummary summary = new OrderSummary(subTotal, shippingFee, vatAmount, totalAmount);

        assertEquals(Double.valueOf(250.00), summary.getSubTotal());
        assertEquals(Double.valueOf(15.00), summary.getShippingFee());
        assertEquals(Double.valueOf(25.00), summary.getVatAmount());
        assertEquals(Double.valueOf(290.00), summary.getTotalAmount());
    }

    @Test
    public void testOrderSummarySettersAndGetters() {
        OrderSummary summary = new OrderSummary();
        summary.setSubTotal(100.0);
        summary.setShippingFee(0.0);
        summary.setVatAmount(10.0);
        summary.setTotalAmount(110.0);

        assertEquals(Double.valueOf(100.0), summary.getSubTotal());
        assertEquals(Double.valueOf(0.0), summary.getShippingFee());
        assertEquals(Double.valueOf(10.0), summary.getVatAmount());
        assertEquals(Double.valueOf(110.0), summary.getTotalAmount());
    }
}
