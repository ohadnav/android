package com.truethat.android.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.databinding.FragmentTheaterBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.custom.ClickableViewPager;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterFragment extends
    MainFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentTheaterBinding>
    implements ScenesPagerFragment.ScenesFetcher, ClickableViewPager.ClickEventListener {
  private TheaterApi mTheaterApi;
  private ScenesPagerFragment mScenesPagerFragment;
  private SavedState mSavedPagerState;

  public static TheaterFragment newInstance() {
    Log.d(TheaterFragment.class.getSimpleName(), "newInstance");
    Bundle args = new Bundle();
    TheaterFragment fragment = new TheaterFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void maybeChangeVisibilityState() {
    super.maybeChangeVisibilityState();
    if (mScenesPagerFragment != null) {
      mScenesPagerFragment.maybeChangeVisibilityState();
    }
  }

  @Override public void onVisible() {
    super.onVisible();
    showToolbar();
    // Adds the scenes pager.
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    mScenesPagerFragment = ScenesPagerFragment.newInstance();
    mScenesPagerFragment.setVisibilityListener(this);
    mScenesPagerFragment.setScenesFetcher(this);
    fragmentTransaction.replace(R.id.theater_scenesPagerLayout, mScenesPagerFragment);
    if (mSavedPagerState != null) {
      mScenesPagerFragment.setInitialSavedState(mSavedPagerState);
    }
    fragmentTransaction.commit();
  }

  @Override public void onHidden() {
    super.onHidden();
    // Kills the scenes pager.
    if (mScenesPagerFragment != null) {
      // Saves the state if the fragment was already added.
      if (getFragmentManager().findFragmentById(R.id.theater_scenesPagerLayout) != null) {
        mSavedPagerState = getFragmentManager().saveFragmentInstanceState(mScenesPagerFragment);
      }
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.remove(mScenesPagerFragment);
      fragmentTransaction.commit();
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Initializes the Theater API
    mTheaterApi = NetworkUtil.createApi(TheaterApi.class);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_theater, getContext());
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mTheaterApi.fetchScenes(AppContainer.getAuthManager().getCurrentUser());
  }

  @Override public void onClickEvent(MotionEvent e) {
    if (mScenesPagerFragment != null) {
      mScenesPagerFragment.onClickEvent(e);
    }
  }
}


