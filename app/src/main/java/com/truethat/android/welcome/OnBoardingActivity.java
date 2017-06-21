package com.truethat.android.welcome;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.VisibleForTesting;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.auth.User;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.util.UiUtil;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.ReactionDetectionPubSub;

public class OnBoardingActivity extends CameraActivity {
  public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  public static final String USER_NAME = "userName";
  @VisibleForTesting static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting static final int VALID_NAME_COLOR = R.color.success;
  private EditText mNameInput;
  private TextWatcher mTextChangedListener = new TextWatcher() {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      stopDetection();
    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override public void afterTextChanged(Editable s) {
      if (User.isValidName(s.toString())) {
        mNameInput.setBackgroundTintList(
            ColorStateList.valueOf(getResources().getColor(VALID_NAME_COLOR, getTheme())));
      } else {
        mNameInput.setBackgroundTintList(
            ColorStateList.valueOf(getResources().getColor(ERROR_COLOR, getTheme())));
      }
    }
  };
  private TextView.OnEditorActionListener mActionListener = new TextView.OnEditorActionListener() {
    @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      boolean handled = false;
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        detectSmile();
        UiUtil.hideSoftKeyboard(OnBoardingActivity.this);
        handled = true;
      }
      return handled;
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_on_boarding);
    mNameInput = (EditText) findViewById(R.id.nameEditText);
    mNameInput.addTextChangedListener(mTextChangedListener);
    mNameInput.setOnEditorActionListener(mActionListener);
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
    }
  }

  @Override protected void onPause() {
    super.onPause();
    stopDetection();
  }

  @Override protected void processImage() {
    // Pushes new input to the detection module.
    App.getReactionDetectionModule().attempt(supplyImage());
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
    finishOnBoarding.putExtra(USER_NAME, mNameInput.getText().toString());
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
      takePicture();
    }
  }
}
