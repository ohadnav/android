package com.truethat.android.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import com.truethat.android.R;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.databinding.ActivityOnBoardingBinding;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.OnBoardingViewModel;
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

  @Override public void onStart() {
    super.onStart();
    mPermissionsManager.requestIfNeeded(this, Permission.CAMERA);
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

  @Override public void showSoftKeyboard() {
    InputMethodManager inputMethodManager =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.showSoftInput(mNameEditText, InputMethodManager.SHOW_FORCED);
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
