package com.truethat.android.view.activity;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.OnBoardingViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import com.truethat.android.viewmodel.viewinterface.OnBoardingViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class OnBoardingActivity extends
    BaseActivity<OnBoardingViewInterface, OnBoardingViewModel, ActivityOnBoardingBinding> implements
    OnBoardingViewInterface {
  @BindView(R.id.nameEditText) EditText mNameEditText;

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
  }

  @Override public void onResume() {
    super.onResume();
    // Maybe we are here by mistake.
    if (mAuthManager.isAuthOk()) {
      finish();
    }
  }

  @Override public void requestNameEditFocus() {
    mNameEditText.requestFocus();
  }

  @Override public void hideSoftKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
  }

  @OnTextChanged(R.id.nameEditText) void onTextChange(CharSequence typedName) {
    getViewModel().mNameEditText.set(typedName.toString());
  }

  @OnEditorAction(R.id.nameEditText) boolean onNameDone(int actionId) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      getViewModel().onNameDone();
    }
    return true;
  }

  @OnFocusChange(R.id.nameEditText) void onNameFocusChange(boolean hasFocus) {
    getViewModel().onNameFocusChange(hasFocus);
  }

  /**
   * Finishes the on boarding flow.
   */
  public void finishOnBoarding() {
    String userFullName = mNameEditText.getText().toString();
    User newUser = new User(StringUtil.extractFirstName(userFullName),
        StringUtil.extractLastName(userFullName), mDeviceManager.getDeviceId(),
        mDeviceManager.getPhoneNumber());
    mAuthManager.signUp(this, newUser);
  }
}
