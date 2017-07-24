package com.truethat.android.common.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Proudly created by ohad on 02/05/2017 for TrueThat.
 */

public class NumberUtil {
  /**
   * Suffixes for various magnitudes (i.e. "k" for thousands).
   */
  private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

  static {
    suffixes.put(1_000L, "K");
    suffixes.put(1_000_000L, "M");
    suffixes.put(1_000_000_000L, "G");
    suffixes.put(1_000_000_000_000L, "T");
    suffixes.put(1_000_000_000_000_000L, "P");
    suffixes.put(1_000_000_000_000_000_000L, "E");
  }

  /**
   * @param value to format
   *
   * @return truncated textual formatting of {@code value}.
   */
  public static String format(long value) {
    //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
    if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
    if (value < 0) return "-" + format(-value);
    if (value < 1000) return Long.toString(value); //deal with easy case

    Map.Entry<Long, String> floorEntry = suffixes.floorEntry(value);
    Long divideBy = floorEntry.getKey();
    String suffix = floorEntry.getValue();

    long truncated = value / (divideBy / 10); //the number part of the output times 10
    boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
    return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
  }

  /**
   * @param map to sum.
   * @param <K> of map keys.
   *
   * @return the sum of the map values.
   */
  @SuppressWarnings("unchecked") public static <K> long sum(Map<K, Long> map) {
    long sum = 0L;
    for (Long l : map.values()) {
      sum += l;
    }
    return sum;
  }
}
