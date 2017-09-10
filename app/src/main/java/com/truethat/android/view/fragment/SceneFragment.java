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
import com.truethat.android.databinding.FragmentSceneBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.SceneViewModel;
import com.truethat.android.viewmodel.viewinterface.SceneViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * A generic container for {@link Scene}. Handles touch gestures for navigation between {@link
 * SceneFragment}, and emotional reaction detection.
 */
public class SceneFragment
    extends BaseFragment<SceneViewInterface, SceneViewModel, FragmentSceneBinding>
    implements SceneViewInterface {
  private static final String ARG_SCENE = "scene";
  @BindView(R.id.reactionImage) ImageView mReactionImage;
  Scene mScene;
  private MediaFragment mMediaFragment;

  public SceneFragment() {
    // Required empty public constructor
  }

  /**
   * @param scene to initialize with
   *
   * @return a fresh from the oven scene fragment. View with care ;)
   */
  public static SceneFragment newInstance(Scene scene) {
    SceneFragment fragment = new SceneFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_SCENE, scene);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mScene = (Scene) getArguments().getSerializable(ARG_SCENE);
    getViewModel().setScene(mScene);
  }

  /**
   * Creation of media layout, such as a Pose image, is done by implementations.
   */
  @SuppressWarnings("unchecked") @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    int mediaContainerViewId = View.generateViewId();
    mRootView.findViewById(R.id.mediaContainer).setId(mediaContainerViewId);
    mMediaFragment = mScene.getMedia().createFragment();
    FragmentTransaction fragmentTransaction =
        getActivity().getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(mediaContainerViewId, mMediaFragment);
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
    return new ViewModelBindingConfig(R.layout.fragment_scene, getContext());
  }

  public Scene getScene() {
    return mScene;
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
