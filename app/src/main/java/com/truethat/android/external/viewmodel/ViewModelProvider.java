package com.truethat.android.external.viewmodel;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import java.util.HashMap;

/**
 * Create and keep this class inside your Activity. Store it
 * in {@link android.support.v4.app.FragmentActivity#onRetainCustomNonConfigurationInstance()
 * and restore in {@link android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)} before
 * calling the super implemenentation.
 */
public class ViewModelProvider {

  @NonNull private final HashMap<String, BaseViewModel<? extends BaseViewInterface>>
      mViewModelCache;

  private ViewModelProvider() {
    mViewModelCache = new HashMap<>();
  }

  @NonNull public static ViewModelProvider newInstance(@NonNull final FragmentActivity activity) {
    if (activity.getLastCustomNonConfigurationInstance() == null) {
      return new ViewModelProvider();
    } else {
      return (ViewModelProvider) activity.getLastCustomNonConfigurationInstance();
    }
  }

  @SuppressWarnings({ "deprecation", "unused" }) @NonNull @Deprecated
  public static ViewModelProvider newInstance(@NonNull final Activity activity) {
    if (activity.getLastNonConfigurationInstance() == null) {
      return new ViewModelProvider();
    } else {
      return (ViewModelProvider) activity.getLastNonConfigurationInstance();
    }
  }

  public synchronized void remove(@Nullable String modeIdentifier) {
    mViewModelCache.remove(modeIdentifier);
  }

  public synchronized void removeAllViewModels() {
    mViewModelCache.clear();
  }

  @SuppressWarnings("unchecked") @NonNull
  public synchronized <ViewInterface extends BaseViewInterface> ViewModelProvider.ViewModelWrapper<ViewInterface> getViewModel(
      @NonNull final String modelIdentifier,
      @NonNull final Class<? extends BaseViewModel<ViewInterface>> viewModelClass) {
    BaseViewModel<ViewInterface> instance =
        (BaseViewModel<ViewInterface>) mViewModelCache.get(modelIdentifier);
    if (instance != null) {
      return new ViewModelWrapper<>(instance);
    }

    try {
      instance = viewModelClass.newInstance();
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
    instance.setUniqueIdentifier(modelIdentifier);
    mViewModelCache.put(modelIdentifier, instance);
    return new ViewModelProvider.ViewModelWrapper<>(instance);
  }

  final static class ViewModelWrapper<ViewInterface extends BaseViewInterface> {
    @NonNull final BaseViewModel<ViewInterface> viewModel;

    private ViewModelWrapper(@NonNull BaseViewModel<ViewInterface> mViewModel) {
      this.viewModel = mViewModel;
    }
  }
}