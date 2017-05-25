package com.truethat.android.common.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 02/05/2017 for TrueThat.
 */
public class NumberTest {
    @Test
    public void format() throws Exception {
        assertEquals("0", Number.format(0));
        assertEquals("5", Number.format(5));
        assertEquals("999", Number.format(999));
        assertEquals("1K", Number.format(1000));
        assertEquals("-5.8K", Number.format(-5821));
        assertEquals("10K", Number.format(10500));
        assertEquals("-101K", Number.format(-101_800));
        assertEquals("2M", Number.format(2_000_000));
        assertEquals("-7.8M", Number.format(-7_800_000));
        assertEquals("92M", Number.format(92_150_000));
        assertEquals("123M", Number.format(123_200_000));
        assertEquals("9.9M", Number.format(9_999_999));
        assertEquals("999P", Number.format(999_999_999_999_999_999L));
        assertEquals("1.2P", Number.format(1_230_000_000_000_000L));
        assertEquals("-9.2E", Number.format(Long.MIN_VALUE));
        assertEquals("9.2E", Number.format(Long.MAX_VALUE));
    }
}