package com.truethat.android.viewmodel;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.google.common.base.Strings;
import com.truethat.android.R;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.viewmodel.viewinterface.OnBoardingViewInterface;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class OnBoardingViewModel extends BaseViewModel<OnBoardingViewInterface>
    implements ReactionDetectionListener {
  @VisibleForTesting public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  @VisibleForTesting @ColorRes public static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting @ColorRes public static final int VALID_NAME_COLOR = R.color.success;
  public final ObservableField<String> mNameEditText = new ObservableField<>();
  public final ObservableInt mNameTextColor = new ObservableInt(R.color.primary);
  public final ObservableBoolean mNameEditCursorVisibility = new ObservableBoolean(true);
  public final ObservableInt mNameEditBackgroundTintColor = new ObservableInt(R.color.hint);
  public final ObservableBoolean mWarningTextVisibility = new ObservableBoolean(false);
  public final ObservableBoolean mCompletionTextVisibility = new ObservableBoolean(false);
  public final ObservableBoolean mCompletionSubscriptTextVisibility = new ObservableBoolean(false);

  @Override public void onBindView(@NonNull OnBoardingViewInterface view) {
    super.onBindView(view);
    mNameEditText.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable sender, int propertyId) {
        onTextChange();
      }
    });
  }

  @Override public void onStart() {
    super.onStart();
    if (isNameValid()) {
      finalStage();
    } else {
      getView().requestNameEditFocus();
    }
    getReactionDetectionManager().start();
  }

  @Override public void onStop() {
    super.onStop();
    getReactionDetectionManager().unsubscribe(this);
    getReactionDetectionManager().stop();
  }

  /**
   * Updates the underline color of {@link #mNameEditText}.
   */
  void onTextChange() {
    mNameEditCursorVisibility.set(true);
    if (StringUtil.isValidFullName(mNameEditText.get())) {
      mWarningTextVisibility.set(false);
      mNameEditBackgroundTintColor.set(VALID_NAME_COLOR);
    } else {
      mNameEditBackgroundTintColor.set(ERROR_COLOR);
    }
  }

  /**
   * The final on-boarding stage, a.k.a asking the user for a real emotion, that is the on-
   * boarding is completed as soon as {@link @REACTION_FOR_DONE} is detected.
   * <p>
   * This shows users the way things go around here.
   */
  private void finalStage() {
    mCompletionTextVisibility.set(true);
    mCompletionSubscriptTextVisibility.set(true);
    // Subscribes to reaction detection.
    getReactionDetectionManager().subscribe(this);
  }

  @Override public void onReactionDetected(Emotion reaction) {
    if (reaction == REACTION_FOR_DONE && StringUtil.isValidFullName(mNameEditText.get())) {
      getView().finishOnBoarding();
    }
  }

  public void onNameDone() {
    if (isNameValid()) {
      finalStage();
    } else {
      mWarningTextVisibility.set(true);
    }
    getView().hideSoftKeyboard();
    mNameEditCursorVisibility.set(false);
  }

  public void onNameFocusChange(boolean hasFocus) {
    mNameEditCursorVisibility.set(hasFocus);
    if (hasFocus) {
      // Undo final stage.
      mCompletionTextVisibility.set(false);
      mCompletionSubscriptTextVisibility.set(false);
      mNameEditCursorVisibility.set(true);
      getReactionDetectionManager().unsubscribe(this);
    } else {
      getView().hideSoftKeyboard();
    }
  }

  private boolean isNameValid() {
    return !Strings.isNullOrEmpty(mNameEditText.get()) && StringUtil.isValidFullName(mNameEditText.get());
  }
}
