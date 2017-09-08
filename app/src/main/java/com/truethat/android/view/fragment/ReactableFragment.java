package com.truethat.android.view.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.ReactableViewModel;
import com.truethat.android.viewmodel.viewinterface.ReactableViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * A generic container for {@link Reactable}. Handles touch gestures for navigation between {@link
 * ReactableFragment}, and emotional reaction detection.
 */
public class ReactableFragment
    extends BaseFragment<ReactableViewInterface, ReactableViewModel, FragmentReactableBinding>
    implements ReactableViewInterface {
  private static final String ARG_REACTABLE = "reactable";
  @BindView(R.id.reactionImage) ImageView mReactionImage;
  Reactable mReactable;
  private MediaFragment mMediaFragment;
  private int mMediaContainerViewId;

  public ReactableFragment() {
    // Required empty public constructor
  }

  /**
   * @param reactable to initialize with
   *
   * @return a fresh from the oven reactable fragment. View with care ;)
   */
  public static ReactableFragment newInstance(Reactable reactable) {
    ReactableFragment fragment = new ReactableFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_REACTABLE, reactable);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mReactable = (Reactable) getArguments().getSerializable(ARG_REACTABLE);
    getViewModel().setReactable(mReactable);
  }

  /**
   * Creation of media layout, such as a Pose image, is done by implementations.
   */
  @SuppressWarnings("unchecked") @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    mMediaContainerViewId = View.generateViewId();
    mRootView.findViewById(R.id.mediaContainer).setId(mMediaContainerViewId);
    mMediaFragment = mReactable.createMediaFragment();
    FragmentTransaction fragmentTransaction =
        getActivity().getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(mMediaContainerViewId, mMediaFragment);
    fragmentTransaction.commit();
    mMediaFragment.setMediaListener(getViewModel());
    return mRootView;
  }

  @Override public void onStart() {
    super.onStart();
    defaultReactionScale();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mMediaFragment = null;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_reactable, getContext());
  }

  public Reactable getReactable() {
    return mReactable;
  }

  @VisibleForTesting public MediaFragment getMediaFragment() {
    return mMediaFragment;
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

  private void defaultReactionScale() {
    mReactionImage.setScaleX(0.5f);
    mReactionImage.setScaleY(0.5f);
  }
}
