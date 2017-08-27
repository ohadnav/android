package com.truethat.android.view.fragment;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.truethat.android.R;
import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.ReactableViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * A generic container for {@link Reactable}. Handles touch gestures for navigation between {@link
 * ReactableFragment}, and emotional reaction detection.
 */
public abstract class ReactableFragment<Model extends Reactable, ViewModel extends ReactableViewModel<Model>, DataBinding extends ViewDataBinding>
    extends BaseFragment<BaseFragmentViewInterface, ViewModel, DataBinding>
    implements BaseFragmentViewInterface {
  private static final String ARG_REACTABLE = "reactable";
  Model mReactable;
  private boolean mDisplayOnly = false;

  public ReactableFragment() {
    // Required empty public constructor
  }

  /**
   * Prepares {@code fragment} for creation in implementing class, such as {@link
   * PoseFragment#newInstance(Pose)}.
   *
   * @param fragment  to prepare.
   * @param reactable to associate with this fragment.
   */
  static void prepareInstance(Fragment fragment, Reactable reactable) {
    Bundle args = new Bundle();
    args.putSerializable(ARG_REACTABLE, reactable);
    fragment.setArguments(args);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mReactable = (Model) getArguments().getSerializable(ARG_REACTABLE);
    getViewModel().setReactable(mReactable);
  }

  /**
   * Creation of media layout, such as a Pose image, is done by implementations.
   */
  @SuppressWarnings("unchecked") @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    inflater.inflate(getMediaFragmentResource(),
        (ViewGroup) mRootView.findViewById(R.id.mediaLayout));
    // Binds views with butterknife.
    mViewUnbinder = ButterKnife.bind(this, mRootView);
    if (mDisplayOnly) getViewModel().displayOnly();
    return mRootView;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_reactable, getContext());
  }

  public Reactable getReactable() {
    return mReactable;
  }

  public void displayOnly() {
    mDisplayOnly = true;
  }

  /**
   * Create the media layout of the fragment, such as the {@link Pose} image.
   */
  abstract @LayoutRes int getMediaFragmentResource();
}
