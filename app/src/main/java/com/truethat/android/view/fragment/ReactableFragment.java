package com.truethat.android.view.fragment;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.truethat.android.R;
import com.truethat.android.di.component.DaggerReactableInjectorComponent;
import com.truethat.android.di.module.ReactableModule;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.ReactableViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * A generic container for {@link Reactable}. Handles touch gestures for navigation between {@link
 * ReactableFragment}, and emotional reaction detection.
 */
public abstract class ReactableFragment<Model extends Reactable, ViewModel extends ReactableViewModel<Model>, DataBinding extends ViewDataBinding>
    extends BaseFragment<BaseFragmentViewInterface, ViewModel, DataBinding>
    implements BaseFragmentViewInterface, ReactableViewModel.ReactionDetectionListener {
  private static final String ARG_REACTABLE = "reactable";

  protected Model mReactable;
  /**
   * Communication interface with parent activity.
   */
  private ReactableViewModel.ReactionDetectionListener mListener;

  public ReactableFragment() {
    // Required empty public constructor
  }

  /**
   * Prepares {@code fragment} for creation in implementing class, such as {@link
   * SceneFragment#newInstance(Scene)}.
   *
   * @param fragment  to prepare.
   * @param reactable to associate with this fragment.
   */
  public static void prepareInstance(Fragment fragment, Reactable reactable) {
    Bundle args = new Bundle();
    args.putSerializable(ARG_REACTABLE, reactable);
    fragment.setArguments(args);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof ReactableViewModel.ReactionDetectionListener) {
      mListener = (ReactableViewModel.ReactionDetectionListener) context;
    }
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mReactable = (Model) getArguments().getSerializable(ARG_REACTABLE);
  }

  /**
   * Creation of media layout, such as a Scene image, is done by implementations i.e. in {@link
   * #createMedia(LayoutInflater)} )}.
   */
  @SuppressWarnings("unchecked") @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    DaggerReactableInjectorComponent.builder()
        .appComponent(getApp().getAppComponent())
        .reactableModule(new ReactableModule(mReactable))
        .build()
        .inject((ReactableViewModel<Reactable>) getViewModel());
    getViewModel().onInjected();
    createMedia(inflater);
    return mRootView;
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_reactable, getContext());
  }

  public Reactable getReactable() {
    return mReactable;
  }

  @Override public void requestDetectionInput() {
    if (mListener == null) {
      throw new RuntimeException(getActivity().getClass().getSimpleName()
          + " must implement "
          + ReactableViewModel.ReactionDetectionListener.class.getSimpleName());
    }
    mListener.requestDetectionInput();
  }

  /**
   * Create the media layout of the fragment, such as the {@link Scene} image.
   */
  @MainThread protected abstract void createMedia(LayoutInflater inflater);
}
