package com.truethat.android.viewmodel;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.InputType;
import android.util.Log;
import com.google.common.base.Strings;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.OnBoardingSignUpStageViewInterface;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class OnBoardingSignUpStageViewModel
    extends BaseFragmentViewModel<OnBoardingSignUpStageViewInterface> {
  @VisibleForTesting @ColorRes public static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting @ColorRes public static final int VALID_COLOR = R.color.success;
  @VisibleForTesting static final int NAME_TEXT_EDITING_INPUT_TYPE =
      InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
  @VisibleForTesting static final int DISABLED_INPUT_TYPE = InputType.TYPE_NULL;
  private static final String BUNDLE_STAGE = "stage";
  private static final String BUNDLE_NAME = "name";
  public final ObservableField<String> mNameEditText = new ObservableField<>();
  public final ObservableInt mNameEditInputType = new ObservableInt(NAME_TEXT_EDITING_INPUT_TYPE);
  public final ObservableInt mNameTextColor = new ObservableInt(R.color.primary);
  public final ObservableBoolean mNameEditCursorVisibility = new ObservableBoolean(true);
  public final ObservableInt mNameEditBackgroundTintColor = new ObservableInt(R.color.hint);
  public final ObservableBoolean mWarningTextVisibility = new ObservableBoolean(false);
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean(false);
  private Stage mStage = Stage.EDIT;

  @Override public void onBindView(@NonNull OnBoardingSignUpStageViewInterface view) {
    super.onBindView(view);
    mNameEditText.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable sender, int propertyId) {
        onNameChange();
      }
    });
  }

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.get(BUNDLE_STAGE) != null) {
        mStage = (Stage) savedInstanceState.getSerializable(BUNDLE_STAGE);
      }
      if (savedInstanceState.get(BUNDLE_NAME) != null) {
        mNameEditText.set(savedInstanceState.getString(BUNDLE_NAME));
      }
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(BUNDLE_STAGE, mStage);
    outState.putString(BUNDLE_NAME, mNameEditText.get());
  }

  @Override public void onVisible() {
    super.onVisible();
    doStage();
  }

  @Override public void onHidden() {
    super.onHidden();
    AppContainer.getAuthManager().cancelRequest();
  }

  public void onNameDone() {
    if (isNameValid()) {
      doSignUp();
    } else {
      mWarningTextVisibility.set(true);
    }
    if (getView() != null) {
      getView().hideSoftKeyboard();
    }
    mNameEditCursorVisibility.set(false);
  }

  public void onNameFocusChange(boolean hasFocus) {
    mNameEditCursorVisibility.set(hasFocus);
    if (hasFocus) {
      if (getView() != null) {
        getView().showSoftKeyboard();
      }
    } else {
      if (getView() != null) {
        getView().hideSoftKeyboard();
      }
    }
  }

  public void failedSignUp() {
    if (getView() != null) {
      getView().showFailedSignUpDialog();
    }
    mWarningTextVisibility.set(true);
    mLoadingImageVisibility.set(false);
    onEdit();
  }

  @VisibleForTesting public Stage getStage() {
    return mStage;
  }

  public void doSignUp() {
    if (!isNameValid()) {
      onEdit();
    }
    Log.d(TAG, "doSignUp");
    mStage = Stage.REQUEST_SENT;
    // Hides soft keyboard and removes focus.
    if (getView() != null) {
      getView().clearNameEditFocus();
      getView().hideSoftKeyboard();
    }
    // Shows load indicator
    mLoadingImageVisibility.set(true);
    // Disable input
    mNameEditInputType.set(DISABLED_INPUT_TYPE);
    // Performs sign up
    String userFullName = mNameEditText.get();
    User newUser = new User(StringUtil.extractFirstName(userFullName),
        StringUtil.extractLastName(userFullName), AppContainer.getDeviceManager().getDeviceId(),
        AppContainer.getDeviceManager().getPhoneNumber());
    if (getView() != null) {
      AppContainer.getAuthManager().signUp(getView().getAuthListener(), newUser);
    }
  }

  private void doStage() {
    switch (mStage) {
      case EDIT:
        onEdit();
        break;
      case REQUEST_SENT:
        doSignUp();
    }
  }

  private void onEdit() {
    Log.d(TAG, "onEdit");
    mStage = Stage.EDIT;
    mNameEditInputType.set(NAME_TEXT_EDITING_INPUT_TYPE);
    if (getView() != null) {
      getView().requestNameEditFocus();
    }
  }

  /**
   * Updates the underline color of {@link #mNameEditText}.
   */
  private void onNameChange() {
    mNameEditCursorVisibility.set(true);
    if (StringUtil.isValidFullName(mNameEditText.get())) {
      mWarningTextVisibility.set(false);
      mNameEditBackgroundTintColor.set(VALID_COLOR);
    } else {
      mNameEditBackgroundTintColor.set(ERROR_COLOR);
    }
  }

  private boolean isNameValid() {
    return !Strings.isNullOrEmpty(mNameEditText.get()) && StringUtil.isValidFullName(
        mNameEditText.get());
  }

  @VisibleForTesting enum Stage {
    /**
     * User edits his name
     */
    EDIT, /**
     * A sign up request is being sent.
     */
    REQUEST_SENT
  }
}
