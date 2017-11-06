package com.truethat.android.view.fragment;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.FragmentScenesPagerBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.custom.SceneFragmentAdapter;
import com.truethat.android.view.custom.VerticalViewPager;
import com.truethat.android.viewmodel.ScenesPagerViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseListener;
import com.truethat.android.viewmodel.viewinterface.ScenesPagerViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public class ScenesPagerFragment
    extends BaseFragment<ScenesPagerViewInterface, ScenesPagerViewModel, FragmentScenesPagerBinding>
    //implements ScenesPagerViewInterface, ClickableViewPager.ClickEventListener {
    implements ScenesPagerViewInterface {
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.scenesPager) VerticalViewPager mPager;
  //private NonSwipableViewPager mPager;
  private SceneFragmentAdapter mSceneFragmentAdapter;
  private ScenesFetcher mScenesFetcher;

  public static ScenesPagerFragment newInstance() {
    Bundle args = new Bundle();
    ScenesPagerFragment fragment = new ScenesPagerFragment();
    fragment.setArguments(args);
    return fragment;
  }

  //@Override public void onClickEvent(MotionEvent e) {
  //  if (e.getX() > AppUtil.realDisplaySize(getContext()).x / 2.0) {
  //    getViewModel().next();
  //  } else {
  //    getViewModel().previous();
  //  }
  //}

  @Override public void maybeChangeVisibilityState() {
    super.maybeChangeVisibilityState();
    if (getCurrentFragment() != null) {
      getCurrentFragment().maybeChangeVisibilityState();
    }
  }

  @Override public void onVisible() {
    super.onVisible();
    AppContainer.getReactionDetectionManager().start(getBaseActivity());
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onHidden() {
    super.onHidden();
    if (getView() != null) {
      ((ViewGroup) getView()).removeView(mPager);
      mPager = null;
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    initializeViewPager();
    return view;
  }

  @Nullable @VisibleForTesting public SceneFragment getCurrentFragment() {
    if (mPager == null
        || mSceneFragmentAdapter == null
        || mPager.getCurrentItem() < 0
        || mPager.getCurrentItem() >= mSceneFragmentAdapter.getCount()) {
      return null;
    }
    return (SceneFragment) mSceneFragmentAdapter.instantiateItem(mPager, mPager.getCurrentItem());
  }

  @Override public void displayItem(int index) {
    if (mPager == null || mSceneFragmentAdapter == null) {
      Log.e(TAG, "Adapter not initialized!");
      return;
    }
    Log.d(TAG, "Displaying scene with index " + index);
    if (mPager.getAdapter().getCount() <= index || index < 0) {
      throw new IndexOutOfBoundsException(
          "Scene index " + index + " is not within scene pager bounds [0, " + mPager.getAdapter()
              .getCount() + ")");
    }
    mPager.setCurrentItem(index, true);
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mScenesFetcher.buildFetchScenesCall();
  }

  @SuppressWarnings("deprecation") @Override public void vibrate() {
    Vibrator vb = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    vb.vibrate(10);
  }

  public void setScenesFetcher(ScenesFetcher scenesFetcher) {
    mScenesFetcher = scenesFetcher;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_scenes_pager, getContext());
  }

  private void initializeViewPager() {
    //if (mPager == null && getView() != null) {
    //  mPager = new NonSwipableViewPager(getContext());
    //  mPager.setId(View.generateViewId());
    //  ConstraintLayout.LayoutParams layoutParams =
    //      new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
    //          ConstraintLayout.LayoutParams.MATCH_PARENT);
    //  ((ViewGroup) getView()).addView(mPager, layoutParams);
    //  ConstraintSet constraintSet = new ConstraintSet();
    //  constraintSet.clone((ConstraintLayout) getView());
    //  // Sets an ID for the fragment container view if it has none.
    //  if (getView().getId() == View.NO_ID) {
    //    getView().setId(View.generateViewId());
    //  }
    //  // Sets the boundaries of the pager to match its parent.
    //  constraintSet.connect(mPager.getId(), ConstraintSet.START, getView().getId(),
    //      ConstraintSet.START);
    //  constraintSet.connect(mPager.getId(), ConstraintSet.END, getView().getId(),
    //      ConstraintSet.END);
    //  constraintSet.connect(mPager.getId(), ConstraintSet.TOP, getView().getId(),
    //      ConstraintSet.TOP);
    //  constraintSet.connect(mPager.getId(), ConstraintSet.BOTTOM, getView().getId(),
    //      ConstraintSet.BOTTOM);
    //  constraintSet.applyTo((ConstraintLayout) getView());
    //  // Initialize the adapter.
    //  mSceneFragmentAdapter = new SceneFragmentAdapter(getFragmentManager(), mPager);
    //  mPager.setAdapter(mSceneFragmentAdapter);
    //  // Visual transformation effects.
    //  mPager.setPageTransformer(true, new ZoomOutPageTransformer());
    //  mPager.setVisibilityListener(this);
    //  // Hooks the view model items to the adapter ones.
    //  mSceneFragmentAdapter.setItems(getViewModel().mItems);
    //}
    mSceneFragmentAdapter = new SceneFragmentAdapter(getFragmentManager(), mPager);
    mPager.setAdapter(mSceneFragmentAdapter);
    mPager.setVisibilityListener(this);
    mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override public void onPageSelected(int position) {
        getViewModel().setDisplayedIndex(position);
      }

      @Override public void onPageScrollStateChanged(int state) {

      }
    });
  }

  public interface ScenesFetcher extends BaseListener {
    Call<List<Scene>> buildFetchScenesCall();
  }
}
