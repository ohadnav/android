package com.truethat.android.view.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.FragmentScenesPagerBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.custom.SceneFragmentAdapter;
import com.truethat.android.viewmodel.ScenesPagerViewModel;
import com.truethat.android.viewmodel.viewinterface.ScenesPagerViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public class ScenesPagerFragment
    extends BaseFragment<ScenesPagerViewInterface, ScenesPagerViewModel, FragmentScenesPagerBinding>
    implements ScenesPagerViewInterface {
  private static final String ARG_DETECT_REACTIONS = "detectReactions";
  @BindView(R.id.scenesPager) ViewPager mPager;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private boolean mDetectReactions = false;
  private SceneFragmentAdapter mSceneFragmentAdapter;
  private ScenePagerListener mListener;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof ScenePagerListener) {
      mListener = (ScenePagerListener) context;
    } else {
      throw new RuntimeException(context.getClass().getSimpleName()
          + " must implement "
          + ScenePagerListener.class.getSimpleName());
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    // Navigation between scenes and activities.
    mRootView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().next();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    mPager.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().next();
      }

      @Override public void onSwipeRight() {
        getViewModel().previous();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    // Initialize views
    mSceneFragmentAdapter = new SceneFragmentAdapter(getActivity().getSupportFragmentManager());
    mPager.setAdapter(mSceneFragmentAdapter);
    // Initializes view model parameters.
    getViewModel().setDetectReactions(mDetectReactions);
    return mRootView;
  }

  @Override public void onStart() {
    super.onStart();
    if (mDetectReactions) {
      AppContainer.getReactionDetectionManager().start(getBaseActivity());
    }
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
    super.onInflate(context, attrs, savedInstanceState);
    TypedArray styledAttributes =
        context.obtainStyledAttributes(attrs, R.styleable.ScenesPagerFragment);
    // Saved state trumps XML.
    if (savedInstanceState == null || !savedInstanceState.getBoolean(ARG_DETECT_REACTIONS)) {
      mDetectReactions =
          styledAttributes.getBoolean(R.styleable.ScenesPagerFragment_detect_reactions, false);
    }
    styledAttributes.recycle();
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      mDetectReactions = savedInstanceState.getBoolean(ARG_DETECT_REACTIONS);
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(ARG_DETECT_REACTIONS, mDetectReactions);
  }

  @VisibleForTesting public SceneFragment getDisplayedScene() {
    return (SceneFragment) mSceneFragmentAdapter.instantiateItem(mPager, mPager.getCurrentItem());
  }

  @Override public void displayItem(int index) {
    mPager.setCurrentItem(index, true);
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mListener.buildFetchScenesCall();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_scenes_pager, getContext());
  }

  public interface ScenePagerListener {
    void onSwipeUp();

    void onSwipeDown();

    Call<List<Scene>> buildFetchScenesCall();
  }
}
