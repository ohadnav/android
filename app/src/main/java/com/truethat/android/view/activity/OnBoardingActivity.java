package com.truethat.android.view.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import com.truethat.android.R;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.databinding.ActivityOnBoardingBinding;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.User;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class OnBoardingActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityOnBoardingBinding>
    implements CameraFragment.OnPictureTakenListener {
  public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  @VisibleForTesting static final int ERROR_COLOR = R.color.error;
  @VisibleForTesting static final int VALID_NAME_COLOR = R.color.success;
  @BindView(R.id.nameEditText) EditText mNameEditText;
  @BindView(R.id.warningText) TextView mWarningText;
  private CameraFragment mCameraFragment;

  @Override public void processImage(Image image) {
    // Pushes new input to the detection module.
    mReactionDetectionManager.attempt(image);
  }

  @Override public void onAuthOk() {
    super.onAuthOk();
    finish();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_on_boarding, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
  }

  @Override public void onResume() {
    super.onResume();
    // Maybe we are here by mistake.
    if (mAuthManager.isAuthOk()) {
      finish();
    }
    // Check if input is already valid.
    if (StringUtil.isValidFullName(mNameEditText.getText().toString())) {
      detectSmile();
    } else {
      mNameEditText.requestFocus();
    }
  }

  @Override public void onPause() {
    super.onPause();
    stopDetection();
  }

  /**
   * Updates the underline color of {@link #mNameEditText}.
   *
   * @param typedName a CharSequence is used thanks to the one and only {@link ButterKnife}.
   */
  @OnTextChanged(R.id.nameEditText) void onTextChange(CharSequence typedName) {
    if (StringUtil.isValidFullName(typedName.toString())) {
      mWarningText.setVisibility(View.INVISIBLE);
      mNameEditText.setBackgroundTintList(
          ColorStateList.valueOf(getResources().getColor(VALID_NAME_COLOR, getTheme())));
    } else {
      mNameEditText.setBackgroundTintList(
          ColorStateList.valueOf(getResources().getColor(ERROR_COLOR, getTheme())));
    }
  }

  @OnEditorAction(R.id.nameEditText) boolean onNameDone(int actionId) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      if (StringUtil.isValidFullName(mNameEditText.getText().toString())) {
        detectSmile();
      } else {
        mWarningText.setVisibility(View.VISIBLE);
      }
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
      mNameEditText.setCursorVisible(false);
    }
    return true;
  }

  @OnFocusChange(R.id.nameEditText) void onNameFocusChange(boolean hasFocus) {
    mNameEditText.setCursorVisible(hasFocus);
    if (!hasFocus) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
    }
  }

  // A method is used since a new instance of an inner class cannot be created in tests.
  @VisibleForTesting OnBoardingReactionDetectionPubSub buildReactionDetectionPubSub() {
    return new OnBoardingReactionDetectionPubSub();
  }

  /**
   * Initiates smile ({@link Emotion#HAPPY} detection, in order to complete the on boarding flow.
   * <p>
   * This is meant to entice users with the way things go around here.
   */
  private void detectSmile() {
    this.runOnUiThread(new Runnable() {
      @Override public void run() {
        findViewById(R.id.smileText).setVisibility(View.VISIBLE);
      }
    });
    // Starts emotional reaction detection. Any previous detection is immediately stopped.
    mReactionDetectionManager.detect(buildReactionDetectionPubSub());
  }

  /**
   * Stops emotional reaction detection that is started by {@link #detectSmile()}. Should be called
   * on activity pauses and {@link #mNameEditText} edits.
   */
  private void stopDetection() {
    this.runOnUiThread(new Runnable() {
      @Override public void run() {
        findViewById(R.id.smileText).setVisibility(View.INVISIBLE);
        findViewById(R.id.realLifeText).setVisibility(View.INVISIBLE);
      }
    });
    // Starts emotional reaction detection. Any previous detection is immediately stopped.
    mReactionDetectionManager.stop();
  }

  /**
   * Finishes the on boarding flow.
   */
  private void finishOnBoarding() {
    String userFullName = mNameEditText.getText().toString();
    User newUser = new User(StringUtil.extractFirstName(userFullName),
        StringUtil.extractLastName(userFullName), mDeviceManager.getDeviceId(),
        mDeviceManager.getPhoneNumber());
    mAuthManager.signUp(this, newUser);
  }

  private class OnBoardingReactionDetectionPubSub implements ReactionDetectionPubSub {
    boolean mFirstInput = true;

    // TODO(ohad): add 2 step reactions.
    @Override public void onReactionDetected(Emotion reaction) {
      if (reaction == REACTION_FOR_DONE) {
        OnBoardingActivity.this.finishOnBoarding();
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
