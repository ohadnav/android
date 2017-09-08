package com.truethat.android.view.activity;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.ActivityOnBoardingBinding;
import com.truethat.android.viewmodel.OnBoardingViewModel;
import com.truethat.android.viewmodel.viewinterface.OnBoardingViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class OnBoardingActivity
    extends BaseActivity<OnBoardingViewInterface, OnBoardingViewModel, ActivityOnBoardingBinding>
    implements OnBoardingViewInterface {
  @BindView(R.id.nameEditText) EditText mNameEditText;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;

  @Override public void onAuthOk() {
    super.onAuthOk();
    finish();
  }

  @Override public void onAuthFailed() {
    Log.d(TAG, "onAuthFailed");
    getViewModel().failedSignUp();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_on_boarding, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }

  @Override public void onResume() {
    super.onResume();
    // Maybe we are here by mistake.
    if (AppContainer.getAuthManager().isAuthOk()) {
      finish();
      return;
    }
    // Plays loading animation.
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
  }

  @Override public void requestNameEditFocus() {
    mNameEditText.requestFocus();
  }

  @Override public void hideSoftKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
  }

  @Override public void showSoftKeyboard() {
    InputMethodManager inputMethodManager =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.showSoftInput(mNameEditText, InputMethodManager.SHOW_FORCED);
  }

  @OnTextChanged(R.id.nameEditText) void onTextChange(CharSequence typedName) {
    getViewModel().mNameEditText.set(typedName.toString());
  }

  @SuppressWarnings("SameReturnValue") @OnEditorAction(R.id.nameEditText) boolean onNameDone(
      int actionId) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      getViewModel().onNameDone();
    }
    return true;
  }

  @OnFocusChange(R.id.nameEditText) void onNameFocusChange(boolean hasFocus) {
    getViewModel().onNameFocusChange(hasFocus);
  }
}
