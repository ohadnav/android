package com.truethat.android.view.fragment;

import android.animation.Animator;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.truethat.android.R;
import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.ReactableViewModel;
import com.truethat.android.viewmodel.viewinterface.ReactableViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * A generic container for {@link Reactable}. Handles touch gestures for navigation between {@link
 * ReactableFragment}, and emotional reaction detection.
 */
public abstract class ReactableFragment<Model extends Reactable, ViewModel extends ReactableViewModel<Model>, DataBinding extends ViewDataBinding>
    extends BaseFragment<ReactableViewInterface, ViewModel, DataBinding>
    implements ReactableViewInterface {
  private static final String ARG_REACTABLE = "reactable";
  @BindView(R.id.reactionImage) ImageView mReactionImage;
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

  @Override public void onStart() {
    super.onStart();
    defaultReactionScale();
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

  @Override public void bounceReactionImage() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mReactionImage.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setListener(new Animator.AnimatorListener() {
              @Override public void onAnimationStart(Animator animation) {

              }

              @Override public void onAnimationEnd(Animator animation) {
                mReactionImage.animate()
                    .scaleX(.5f)
                    .scaleY(.5f)
                    .setListener(new Animator.AnimatorListener() {
                      @Override public void onAnimationStart(Animator animation) {

                      }

                      @Override public void onAnimationEnd(Animator animation) {

                      }

                      @Override public void onAnimationCancel(Animator animation) {
                        defaultReactionScale();
                      }

                      @Override public void onAnimationRepeat(Animator animation) {

                      }
                    })
                    .start();
              }

              @Override public void onAnimationCancel(Animator animation) {
                defaultReactionScale();
              }

              @Override public void onAnimationRepeat(Animator animation) {

              }
            })
            .start();
      }
    });
  }

  /**
   * Create the media layout of the fragment, such as the {@link Pose} image.
   */
  abstract @LayoutRes int getMediaFragmentResource();

  private void defaultReactionScale() {
    mReactionImage.setScaleX(0.5f);
    mReactionImage.setScaleY(0.5f);
  }
}
