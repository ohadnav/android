package com.truethat.android.common.util;

import com.google.common.base.Strings;

/**
 * Proudly created by ohad on 05/07/2017 for TrueThat.
 */

public class StringUtil {
  public static String toTitleCase(String s) {
    String[] words = s.split(" ");
    StringBuilder builder = new StringBuilder();

    for (String word : words) {
      if (!Strings.isNullOrEmpty(word)) {
        builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
      }
    }
    return builder.toString().trim();
  }
}
