package com.truethat.android.view.fragment;

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
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.databinding.FragmentOnBoardingSignUpBinding;
import com.truethat.android.viewmodel.OnBoardingSignUpStageViewModel;
import com.truethat.android.viewmodel.viewinterface.OnBoardingListener;
import com.truethat.android.viewmodel.viewinterface.OnBoardingSignUpStageViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 */

public class OnBoardingSignUpStageFragment extends
    BaseFragment<OnBoardingSignUpStageViewInterface, OnBoardingSignUpStageViewModel, FragmentOnBoardingSignUpBinding>
    implements OnBoardingSignUpStageViewInterface, AuthListener {
  @BindView(R.id.nameEditText) EditText mNameEditText;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private OnBoardingListener mOnBoardingListener;

  public static OnBoardingSignUpStageFragment newInstance() {
    Bundle args = new Bundle();
    OnBoardingSignUpStageFragment fragment = new OnBoardingSignUpStageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onAuthOk() {
    mOnBoardingListener.nextStage();
  }

  @Override public void onAuthFailed() {
    Log.d(TAG, "onAuthFailed");
    getViewModel().failedSignUp();
  }

  @Override public void onVisible() {
    super.onVisible();
    // Plays loading animation.
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
  }

  @Override public void onHidden() {
    super.onHidden();
    if (mNameEditText != null) {
      hideSoftKeyboard();
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnBoardingListener) {
      mOnBoardingListener = (OnBoardingListener) context;
    } else {
      throw new IllegalStateException(
          "Fragments' context must implement " + OnBoardingListener.class.getSimpleName());
    }
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_on_boarding_sign_up, getContext());
  }

  @Override public void requestNameEditFocus() {
    mNameEditText.requestFocus();
  }

  @Override public void clearNameEditFocus() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mNameEditText.clearFocus();
      }
    });
  }

  @Override public void hideSoftKeyboard() {
    if (getActivity() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
    }
  }

  @Override public void showSoftKeyboard() {
    InputMethodManager inputMethodManager =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.showSoftInput(mNameEditText, InputMethodManager.SHOW_FORCED);
  }

  @Override public AuthListener getAuthListener() {
    return this;
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
