package com.truethat.android.view.fragment;

import android.content.Context;
import android.databinding.ViewDataBinding;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.OnBoardingListener;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */

public abstract class OnBoardingStageFragment<ViewInterface extends BaseFragmentViewInterface, ViewModel extends BaseFragmentViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends BaseFragment<ViewInterface, ViewModel, DataBinding> {
  OnBoardingListener mOnBoardingListener;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnBoardingListener) {
      mOnBoardingListener = (OnBoardingListener) context;
    } else {
      throw new IllegalStateException(
          "Fragments' context must implement " + OnBoardingListener.class.getSimpleName());
    }
  }
}
