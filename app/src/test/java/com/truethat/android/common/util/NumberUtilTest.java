package com.truethat.android.common.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 02/05/2017 for TrueThat.
 */
public class NumberUtilTest {
  @Test public void format() throws Exception {
    assertEquals("0", NumberUtil.format(0));
    assertEquals("5", NumberUtil.format(5));
    assertEquals("999", NumberUtil.format(999));
    assertEquals("1K", NumberUtil.format(1000));
    assertEquals("-5.8K", NumberUtil.format(-5821));
    assertEquals("10K", NumberUtil.format(10500));
    assertEquals("-101K", NumberUtil.format(-101_800));
    assertEquals("2M", NumberUtil.format(2_000_000));
    assertEquals("-7.8M", NumberUtil.format(-7_800_000));
    assertEquals("92M", NumberUtil.format(92_150_000));
    assertEquals("123M", NumberUtil.format(123_200_000));
    assertEquals("9.9M", NumberUtil.format(9_999_999));
    assertEquals("999P", NumberUtil.format(999_999_999_999_999_999L));
    assertEquals("1.2P", NumberUtil.format(1_230_000_000_000_000L));
    assertEquals("-9.2E", NumberUtil.format(Long.MIN_VALUE));
    assertEquals("9.2E", NumberUtil.format(Long.MAX_VALUE));
  }

  @Test public void sum() throws Exception {
    assertEquals(2L, NumberUtil.sum(ImmutableMap.of("a", 1L, "b", 1L)));
  }
}