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
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.OnBoardingSignUpStageViewInterface;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class OnBoardingSignUpStageViewModel
    extends BaseFragmentViewModel<OnBoardingSignUpStageViewInterface>
    implements ReactionDetectionListener {
  @VisibleForTesting public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  @VisibleForTesting @ColorRes public static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting @ColorRes public static final int VALID_NAME_COLOR = R.color.success;
  @VisibleForTesting static final int NAME_TEXT_EDITING_INPUT_TYPE =
      InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
  @VisibleForTesting static final int NAME_TEXT_DISABLED_INPUT_TYPE = InputType.TYPE_NULL;
  private static final String BUNDLE_STAGE = "stage";
  private static final String BUNDLE_NAME = "name";
  public final ObservableField<String> mNameEditText = new ObservableField<>();
  public final ObservableInt mNameEditInputType = new ObservableInt(NAME_TEXT_EDITING_INPUT_TYPE);
  public final ObservableInt mNameTextColor = new ObservableInt(R.color.primary);
  public final ObservableBoolean mNameEditCursorVisibility = new ObservableBoolean(true);
  public final ObservableInt mNameEditBackgroundTintColor = new ObservableInt(R.color.hint);
  public final ObservableBoolean mWarningTextVisibility = new ObservableBoolean(false);
  public final ObservableField<String> mWarningText = new ObservableField<>();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean(false);
  public final ObservableBoolean mCompletionTextVisibility = new ObservableBoolean(false);
  public final ObservableBoolean mCompletionSubscriptTextVisibility = new ObservableBoolean(false);
  private Stage mStage = Stage.EDIT;

  @Override public void onBindView(@NonNull OnBoardingSignUpStageViewInterface view) {
    super.onBindView(view);
    mNameEditText.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable sender, int propertyId) {
        onTextChange();
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
    AppContainer.getReactionDetectionManager().unsubscribe(this);
    AppContainer.getReactionDetectionManager().stop();
    AppContainer.getAuthManager().cancelRequest();
  }

  @Override public void onReactionDetected(Emotion reaction, boolean mostLikely) {
    if (REACTION_FOR_DONE.equals(reaction) && StringUtil.isValidFullName(mNameEditText.get())) {
      onRequestSent();
    }
  }

  @Override public void onFaceDetectionStarted() {

  }

  @Override public void onFaceDetectionStopped() {

  }

  public void onNameDone() {
    if (isNameValid()) {
      onFinalStage();
    } else {
      mWarningText.set(getContext().getString(R.string.name_edit_warning_text));
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
      // Undo final stage.
      mCompletionTextVisibility.set(false);
      mCompletionSubscriptTextVisibility.set(false);
      AppContainer.getReactionDetectionManager().unsubscribe(this);
      if (getView() != null) {
        getView().showSoftKeyboard();
      }
    } else {
      if (getView() != null) {
        getView().hideSoftKeyboard();
      }
      if (isNameValid()) {
        onFinalStage();
      }
    }
  }

  public void failedSignUp() {
    mWarningText.set(getContext().getString(R.string.sign_up_failed_warning_text));
    mWarningTextVisibility.set(true);
    mLoadingImageVisibility.set(false);
    onEdit();
  }

  @VisibleForTesting public Stage getStage() {
    return mStage;
  }

  private void doStage() {
    switch (mStage) {
      case EDIT:
        onEdit();
        break;
      case FINAL:
        onFinalStage();
        break;
      case REQUEST_SENT:
        onRequestSent();
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
   * The final on-boarding stage, a.k.a asking the user for a real emotion, that is the on-
   * boarding is completed as soon as {@link @REACTION_FOR_DONE} is detected.
   * <p>
   * This shows users the way things go around here.
   */
  private void onFinalStage() {
    if (!isNameValid()) {
      onEdit();
    }
    Log.d(TAG, "onFinalStage");
    mStage = Stage.FINAL;
    // TODO: use a cleaner way to achieve the same result.
    // Timer is used because the stage can be launched when the keyboard or focus controls are not available.
    new Timer().schedule(new TimerTask() {
      @Override public void run() {
        if (getView() != null) {
          getView().clearNameEditFocus();
          getView().hideSoftKeyboard();
        }
      }
    }, 100);
    mLoadingImageVisibility.set(false);
    mNameEditInputType.set(NAME_TEXT_EDITING_INPUT_TYPE);
    mCompletionTextVisibility.set(true);
    mCompletionSubscriptTextVisibility.set(true);
    // Hide warnings
    mWarningTextVisibility.set(false);
    // Starts detection.
    if (getView() != null) {
      AppContainer.getReactionDetectionManager().start(getView().getBaseActivity());
    }
    // Subscribes to reaction detection.
    AppContainer.getReactionDetectionManager().subscribe(this);
  }

  private void onRequestSent() {
    if (!isNameValid()) {
      onEdit();
    }
    Log.d(TAG, "onRequestSent");
    mStage = Stage.REQUEST_SENT;
    // Hides soft keyboard and removes focus.
    if (getView() != null) {
      getView().clearNameEditFocus();
      getView().hideSoftKeyboard();
    }
    // Shows load indicator
    mLoadingImageVisibility.set(true);
    // Disable input
    mNameEditInputType.set(NAME_TEXT_DISABLED_INPUT_TYPE);
    // Unsubscribes to reaction detection, to avoid multiple sign ups
    AppContainer.getReactionDetectionManager().unsubscribe(this);
    // Performs sign up
    String userFullName = mNameEditText.get();
    User newUser = new User(StringUtil.extractFirstName(userFullName),
        StringUtil.extractLastName(userFullName), AppContainer.getDeviceManager().getDeviceId(),
        AppContainer.getDeviceManager().getPhoneNumber());
    if (getView() != null) {
      AppContainer.getAuthManager().signUp(getView().getAuthListener(), newUser);
    }
  }

  /**
   * Updates the underline color of {@link #mNameEditText}.
   */
  private void onTextChange() {
    mNameEditCursorVisibility.set(true);
    if (StringUtil.isValidFullName(mNameEditText.get())) {
      mWarningTextVisibility.set(false);
      mNameEditBackgroundTintColor.set(VALID_NAME_COLOR);
    } else {
      mNameEditBackgroundTintColor.set(ERROR_COLOR);
    }
  }

  private boolean isNameValid() {
    return !Strings.isNullOrEmpty(mNameEditText.get()) && StringUtil.isValidFullName(
        mNameEditText.get());
  }

  @VisibleForTesting public enum Stage {
    /**
     * User edits his name
     */
    EDIT, /**
     * User has reached the final stage, and we await for detection of {@link #REACTION_FOR_DONE}.
     */
    FINAL, /**
     * A sign up request is being sent.
     */
    REQUEST_SENT
  }
}
