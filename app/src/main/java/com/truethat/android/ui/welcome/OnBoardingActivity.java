package com.truethat.android.ui.welcome;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.User;
import com.truethat.android.ui.common.BaseActivity;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.common.util.UiUtil;

public class OnBoardingActivity extends BaseActivity
    implements CameraFragment.OnPictureTakenListener {
  public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  public static final String USER_NAME_INTENT = "userName";
  @VisibleForTesting static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting static final int VALID_NAME_COLOR = R.color.success;
  @BindView(R.id.nameEditText) EditText mNameInput;
  private CameraFragment mCameraFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_on_boarding;
  }

  @Override protected void onResume() {
    super.onResume();
    // Maybe we are here by mistake.
    if (App.getAuthModule().isAuthOk()) {
      finish();
    }
    // Check if input is already valid.
    if (User.isValidName(mNameInput.getText().toString())) {
      detectSmile();
    } else {
      mNameInput.requestFocus();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    stopDetection();
  }

  @Override public void processImage(Image image) {
    // Pushes new input to the detection module.
    App.getReactionDetectionModule().attempt(image);
  }

  /**
   * Updates the underline color of {@link #mNameInput}.
   *
   * @param typedName a CharSequence is used thanks to the one and only {@link ButterKnife}.
   */
  @OnTextChanged(R.id.nameEditText) void updateColor(CharSequence typedName) {
    if (User.isValidName(typedName.toString())) {
      mNameInput.setBackgroundTintList(
          ColorStateList.valueOf(getResources().getColor(VALID_NAME_COLOR, getTheme())));
    } else {
      mNameInput.setBackgroundTintList(
          ColorStateList.valueOf(getResources().getColor(ERROR_COLOR, getTheme())));
    }
  }

  @OnEditorAction(R.id.nameEditText) boolean onNameDone(int actionId) {
    boolean handled = false;
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      detectSmile();
      UiUtil.hideSoftKeyboard(OnBoardingActivity.this);
      handled = true;
    }
    return handled;
  }

  /**
   * Initiates smile ({@link Emotion#HAPPY} detection, in order to complete the on boarding flow.
   *
   * This is meant to entice users with the way things go around here.
   */
  private void detectSmile() {
    this.runOnUiThread(new Runnable() {
      @Override public void run() {
        findViewById(R.id.smileText).setVisibility(View.VISIBLE);
      }
    });
    // Starts emotional reaction detection. Any previous detection is immediately stopped.
    App.getReactionDetectionModule().detect(buildReactionDetectionPubSub());
  }

  /**
   * Stops emotional reaction detection that is started by {@link #detectSmile()}. Should be called
   * on activity pauses and {@link #mNameInput} edits.
   */
  private void stopDetection() {
    this.runOnUiThread(new Runnable() {
      @Override public void run() {
        findViewById(R.id.smileText).setVisibility(View.GONE);
        findViewById(R.id.realLifeText).setVisibility(View.GONE);
      }
    });
    // Starts emotional reaction detection. Any previous detection is immediately stopped.
    App.getReactionDetectionModule().stop();
  }

  /**
   * Finishes the on boarding flow.
   */
  @MainThread private void finishOnBoarding() {
    Intent finishOnBoarding = new Intent();
    finishOnBoarding.putExtra(USER_NAME_INTENT, mNameInput.getText().toString());
    setResult(RESULT_OK, finishOnBoarding);
    finish();
  }

  // A method is used since a new instance of an inner class cannot be created in tests.
  @VisibleForTesting OnBoardingReactionDetectionPubSub buildReactionDetectionPubSub() {
    return new OnBoardingReactionDetectionPubSub();
  }

  private class OnBoardingReactionDetectionPubSub implements ReactionDetectionPubSub {
    boolean mFirstInput = true;

    @Override public void onReactionDetected(Emotion reaction) {
      if (reaction == REACTION_FOR_DONE) {
        OnBoardingActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            OnBoardingActivity.this.finishOnBoarding();
          }
        });
      } else {
        detectSmile();
      }
    }

    @Override public void requestInput() {
      if (!mFirstInput) {
        OnBoardingActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            OnBoardingActivity.this.findViewById(R.id.realLifeText).setVisibility(View.VISIBLE);
          }
        });
      }
      mFirstInput = false;
      mCameraFragment.takePicture();
    }
  }
}
