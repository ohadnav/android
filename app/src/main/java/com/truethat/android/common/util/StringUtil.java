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

  /**
   * Valid names satisfy the following conditions:
   * <ul>
   * <li>They only contain english letters and spaces.</li>
   * <li>They have both first and last name.</li>
   * <li>Both first and last are at least 2 letters long.</li>
   * </ul>
   *
   * @return whether the given name can formulate first and last names for the user.
   */
  public static boolean isValidFullName(String fullName) {
    fullName = fullName.toLowerCase().trim();
    boolean isAlphabetic = fullName.matches("[a-z\\s]*");
    String firstName = extractFirstName(fullName);
    String lastName = extractLastName(fullName);
    // One letter names are invalid.
    boolean isFirstNameValid = firstName.length() > 1;
    boolean isLastNameValid = lastName.length() > 1;
    return isAlphabetic && isFirstNameValid && isLastNameValid;
  }

  /**
   * @param fullName of a happy human being
   *
   * @return the first word of the name.
   */
  public static String extractFirstName(String fullName) {
    return fullName.split(" ")[0].trim().toLowerCase();
  }

  /**
   * @param fullName of a Game of Thrones loving person.
   *
   * @return the entire {@code fullName} but its first word.
   */
  public static String extractLastName(String fullName) {
    String lastName = "";
    if (fullName.contains(" ")) {
      lastName = fullName.substring(fullName.indexOf(" ")).trim();
    }
    return lastName.toLowerCase();
  }
}
