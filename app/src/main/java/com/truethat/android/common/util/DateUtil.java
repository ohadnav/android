package com.truethat.android.common.util;

import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */

public class DateUtil {
  /**
   * Textual description of recent times.
   */
  static final String NOW = "now";
  /**
   * Time threshold to consider a time difference as "now".
   */
  private static final int MAX_SECONDS_TO_CONSIDER_AS_NOW = 40;
  /**
   * Suffixes for time differences magnitudes (i.e. minutes, hours etc.)
   */
  private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

  static {
    suffixes.put(TimeUnit.MINUTES.toMillis(1), "m");
    suffixes.put(TimeUnit.HOURS.toMillis(1), "h");
    suffixes.put(TimeUnit.DAYS.toMillis(1), "d");
    suffixes.put(TimeUnit.DAYS.toMillis(1) * 30, "mon");
    suffixes.put(TimeUnit.DAYS.toMillis(1) * 365, "yr");
  }

  /**
   * @param than date from which to calculate difference to current time.
   *
   * @return human readable difference between now and {@code than}.
   */
  public static String formatTimeAgo(Date than) {
    long diff = new Date().getTime() - than.getTime();

    if (diff < 0) throw new IllegalArgumentException("than parameter must be from the past.");
    // If the diff is small enough, then consider it as "now"
    if (diff < TimeUnit.SECONDS.toMillis(MAX_SECONDS_TO_CONSIDER_AS_NOW)) return NOW;
    // Find proper time unit.
    Map.Entry<Long, String> floorTimeUnit = suffixes.floorEntry(diff);
    // Milliseconds to divide by the difference.
    Long divideBy = floorTimeUnit.getKey();
    String suffix = floorTimeUnit.getValue();

    // Quantity of selected time unit.
    long truncated = diff / divideBy;
    return truncated + suffix + " ago";
  }
}
