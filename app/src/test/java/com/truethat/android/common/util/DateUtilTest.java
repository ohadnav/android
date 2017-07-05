package com.truethat.android.common.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class DateUtilTest {
  @Test public void formatTimeAgo() throws Exception {
    Date now = new Date();
    Date halfMinuteAgo = new Date(now.getTime() - TimeUnit.SECONDS.toMillis(30));
    assertEquals(DateUtil.NOW, DateUtil.formatTimeAgo(halfMinuteAgo));
    Date oneMinuteAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(1));
    assertEquals("1m ago", DateUtil.formatTimeAgo(oneMinuteAgo));
    Date twoMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(2));
    assertEquals("2m ago", DateUtil.formatTimeAgo(twoMinutesAgo));
    Date almostHourAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(59));
    assertEquals("59m ago", DateUtil.formatTimeAgo(almostHourAgo));
    Date hourAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(1));
    assertEquals("1h ago", DateUtil.formatTimeAgo(hourAgo));
    Date dayAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
    assertEquals("1d ago", DateUtil.formatTimeAgo(dayAgo));
    Date monthAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(30));
    assertEquals("1mon ago", DateUtil.formatTimeAgo(monthAgo));
    Date yearAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(365));
    assertEquals("1yr ago", DateUtil.formatTimeAgo(yearAgo));
  }
}