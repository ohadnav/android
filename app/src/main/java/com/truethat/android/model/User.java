package com.truethat.android.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.application.App;
import com.truethat.android.application.DeviceManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/User.java
 */

public class User implements Serializable {
  /**
   * User session path, within the app internal storage.
   */
  @VisibleForTesting public static final String LAST_USER_PATH = "users/last.user";
  /**
   * User ID, as stored in our backend.
   */
  private Long mId;
  /**
   * First name.
   */
  private String mFirstName;
  /**
   * Last name.
   */
  private String mLastName;
  /**
   * Android ID.
   */
  private String mDeviceId;
  /**
   * Current phone number.
   */
  private String mPhoneNumber;

  @VisibleForTesting
  public User(@Nullable Long id, @Nullable String firstName, @Nullable String lastName) {
    mId = id;
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = App.getDeviceManager().getDeviceId(null);
    mPhoneNumber = App.getDeviceManager().getPhoneNumber(null);
  }

  /**
   * @param context to utilize internal storage and {@link DeviceManager}.
   */
  public User(Context context) throws IOException, ClassNotFoundException {
    // Trying to retrieve previous session from internal storage.
    if (App.getInternalStorage().exists(context, LAST_USER_PATH)) {
      populateFromInternalStorage(context);
    }
    mDeviceId = App.getDeviceManager().getDeviceId(context);
    mPhoneNumber = App.getDeviceManager().getPhoneNumber(context);
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
  public static boolean isValidName(String name) {
    name = name.toLowerCase().trim();
    boolean isAlphabetic = name.matches("[a-z\\s]*");
    String firstName = extractFirstName(name);
    String lastName = extractLastName(name);
    // One letter names are invalid.
    boolean isFirstNameValid = firstName.length() > 1;
    boolean isLastNameValid = lastName.length() > 1;
    return isAlphabetic && isFirstNameValid && isLastNameValid;
  }

  @VisibleForTesting static String extractFirstName(String name) {
    return name.split(" ")[0].trim().toLowerCase();
  }

  @VisibleForTesting static String extractLastName(String name) {
    String lastName = "";
    if (name.contains(" ")) {
      lastName = name.substring(name.indexOf(" ")).trim();
    }
    return lastName.toLowerCase();
  }

  public long getId() {
    if (mId == null) {
      throw new IllegalStateException("User ID had not been initialized.");
    }
    return mId;
  }

  public void setId(Long id) {
    mId = id;
  }

  public String getFirstName() {
    return mFirstName;
  }

  public String getLastName() {
    return mLastName;
  }

  public String getDisplayName() {
    if (mFirstName == null || mLastName == null) {
      throw new IllegalStateException("User had not been initialized.");
    }
    return mFirstName + " " + mLastName;
  }

  /**
   * Whether this User is seemingly have been through authentication.
   *
   * @return whether ID, first and last names are all non-null.
   */
  public boolean isAuthOk() {
    return hasId() && onBoarded();
  }

  /**
   * Updates this User {@code mFirstName} and {@code mLastName}, and then saves the entire instance
   * to internal storage.
   *
   * @param name    full name
   * @param context for internal storage.
   */
  public void updateNames(String name, Context context) throws IOException {
    name = name.toLowerCase().trim().replaceAll(" +", " ");
    if (isValidName(name)) {
      mFirstName = extractFirstName(name);
      mLastName = extractLastName(name);
      if (isAuthOk()) save(context);
    }
  }

  public boolean hasId() {
    return mId != null;
  }

  /**
   * Updates this user instance using another one. Only updates {@code mId} and {@code mName}.
   *
   * @param user    to copy fields from.
   * @param context to access internal storage.
   */
  public void update(User user, Context context) throws IOException {
    boolean modified = false;
    if (user.mFirstName != null && !Objects.equals(mFirstName, user.mFirstName)) {
      mFirstName = user.mFirstName;
      modified = true;
    }
    if (user.mLastName != null && !Objects.equals(mLastName, user.mLastName)) {
      mLastName = user.mLastName;
      modified = true;
    }
    if (user.mId != null && !Objects.equals(mId, user.mId)) {
      mId = user.mId;
      modified = true;
    }
    if (modified) {
      save(context);
    }
  }

  /**
   * Saves this user to internal storage, for faster application bootstrap in future sessions.
   *
   * @param context to access internal storage.
   */
  public void save(Context context) throws IOException {
    App.getInternalStorage().write(context, LAST_USER_PATH, this);
  }

  /**
   * @return whether the user had been through on boarding.
   */
  public boolean onBoarded() {
    return mFirstName != null && mLastName != null;
  }

  @Override public int hashCode() {
    return mId.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;

    User user = (User) o;

    return mId.equals(user.mId);
  }

  /**
   * Populates this User fields based on ones found in {@link #LAST_USER_PATH}.
   *
   * @param context to access internal storage
   */
  private void populateFromInternalStorage(Context context)
      throws IOException, ClassNotFoundException {
    User lastUser = App.getInternalStorage().read(context, LAST_USER_PATH);
    mId = lastUser.mId;
    mFirstName = lastUser.mFirstName;
    mLastName = lastUser.mLastName;
    mDeviceId = lastUser.mDeviceId;
    mPhoneNumber = lastUser.mPhoneNumber;
  }
}
