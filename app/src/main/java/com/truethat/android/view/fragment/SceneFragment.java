package com.truethat.android.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.common.util.StyleUtil;
import com.truethat.android.databinding.FragmentSceneBinding;
import com.truethat.android.model.Media;
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
  @BindView(R.id.reactionsCountLayout) ConstraintLayout mReactionsLayout;
  private Scene mScene;
  private MediaFragment mMediaFragment;
  private Integer mMediaContainerViewId = View.generateViewId();

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

  @Override public void maybeChangeVisibilityState() {
    super.maybeChangeVisibilityState();
    if (mMediaFragment != null) {
      mMediaFragment.maybeChangeVisibilityState();
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mScene = (Scene) getArguments().getSerializable(ARG_SCENE);
    if (mScene != null) {
      TAG += " " + mScene.getId();
    }
    getViewModel().setScene(mScene);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (getView() == null) {
      throw new IllegalStateException("Fragment root view should have already been created.");
    }
    getView().findViewById(R.id.mediaContainer).setId(mMediaContainerViewId);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mMediaFragment = null;
  }

  @Override public boolean shouldBeVisible(Object o) {
    if (o instanceof MediaFragment) {
      MediaFragment mediaFragment = (MediaFragment) o;
      if (getViewModel().getCurrentMedia() == null || !getViewModel().getCurrentMedia()
          .equals(mediaFragment.getMedia())) {
        return false;
      }
    }
    return super.shouldBeVisible(o);
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
        StyleUtil.bounceIn(mReactionImage, null);
      }
    });
  }

  @Override public void display(Media media) {
    Log.d(TAG, "Displaying " + media);
    mMediaFragment = media.createFragment();
    mMediaFragment.setVisibilityListener(this);
    FragmentTransaction fragmentTransaction =
        getActivity().getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(mMediaContainerViewId, mMediaFragment);
    fragmentTransaction.commit();
    mMediaFragment.setMediaListener(getViewModel());
  }

  @Override public boolean hasMediaFinished() {
    return mMediaFragment != null && mMediaFragment.hasFinished();
  }

  @Override public void fadeReactions() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mReactionsLayout.animate().alpha(0.4f).setDuration(100).start();
      }
    });
  }

  @Override public void exposeReactions() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mReactionsLayout.animate().alpha(1f).setDuration(100).start();
      }
    });
  }
}
