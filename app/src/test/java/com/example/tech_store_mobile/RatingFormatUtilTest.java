package com.example.tech_store_mobile;

import com.example.tech_store_mobile.utils.RatingFormatUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class RatingFormatUtilTest {

    @Test
    public void testRoundToTenth_normalValues() {
        assertEquals(4.5, RatingFormatUtil.roundToTenth(4.53), 0.001);
        assertEquals(4.6, RatingFormatUtil.roundToTenth(4.56), 0.001);
        assertEquals(5.0, RatingFormatUtil.roundToTenth(4.99), 0.001);
        assertEquals(0.0, RatingFormatUtil.roundToTenth(0.0), 0.001);
    }

    @Test
    public void testRoundToTenth_nullAndSpecialValues() {
        assertEquals(0.0, RatingFormatUtil.roundToTenth(null), 0.001);
        assertEquals(0.0, RatingFormatUtil.roundToTenth(Double.NaN), 0.001);
        assertEquals(0.0, RatingFormatUtil.roundToTenth(Double.POSITIVE_INFINITY), 0.001);
    }

    @Test
    public void testFormatRating_standard() {
        assertEquals("4.5", RatingFormatUtil.formatRating(4.53));
        assertEquals("5.0", RatingFormatUtil.formatRating(5.0));
        assertEquals("0.0", RatingFormatUtil.formatRating(null));
    }

    @Test
    public void testFormatRatingWithSuffix() {
        assertEquals("4.8 ★", RatingFormatUtil.formatRatingWithSuffix(4.78, " ★"));
        assertEquals("5.0 (20 reviews)", RatingFormatUtil.formatRatingWithSuffix(4.98, " (20 reviews)"));
    }
}
