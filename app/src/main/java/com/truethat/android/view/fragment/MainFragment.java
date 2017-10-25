package com.truethat.android.view.fragment;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import com.truethat.android.view.activity.MainActivity;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.ToolbarViewInterface;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

public abstract class MainFragment<ViewInterface extends BaseFragmentViewInterface, ViewModel extends BaseFragmentViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends BaseFragment<ViewInterface, ViewModel, DataBinding> implements ToolbarViewInterface {

  @CallSuper @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof MainActivity)) {
      throw new IllegalStateException("MainFragment can only be attached to MainActivity.");
    }
  }

  @Override public void navigateToTheater() {
    getMainActivity().navigateToTheater();
  }

  @Override public void navigateToStudio() {
    getMainActivity().navigateToStudio();
  }

  @Override public void navigateToRepertoire() {
    getMainActivity().navigateToRepertoire();
  }

  @Override public void hideToolbar() {
    getMainActivity().hideToolbar();
  }

  @Override public void showToolbar() {
    getMainActivity().showToolbar();
  }

  private MainActivity getMainActivity() {
    return (MainActivity) getActivity();
  }
}
