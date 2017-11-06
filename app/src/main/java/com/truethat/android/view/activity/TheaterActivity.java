package com.truethat.android.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.databinding.ActivityTheaterBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.ScenesPagerFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends
    MainActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTheaterBinding>
    implements ScenesPagerFragment.ScenesFetcher {
  private TheaterApi mTheaterApi;
  private Fragment.SavedState mSavedPagerState;

  //public static TheaterActivity newInstance() {
  //  Log.d(TheaterActivity.class.getSimpleName(), "newInstance");
  //  Bundle args = new Bundle();
  //  TheaterActivity fragment = new TheaterActivity();
  //  fragment.setArguments(args);
  //  return fragment;
  //}

  //@Override public void maybeChangeVisibilityState() {
  //  super.maybeChangeVisibilityState();
  //  if (mScenesPagerFragment != null) {
  //    mScenesPagerFragment.maybeChangeVisibilityState();
  //  }
  //}

  //@Override public void onVisible() {
  //  super.onVisible();
  //  showToolbar();
  //  // Adds the scenes pager.
  //  FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
  //  mScenesPagerFragment = ScenesPagerFragment.newInstance();
  //  mScenesPagerFragment.setVisibilityListener(this);
  //  mScenesPagerFragment.setScenesFetcher(this);
  //  fragmentTransaction.replace(R.id.theater_scenesPagerLayout, mScenesPagerFragment);
  //  if (mSavedPagerState != null) {
  //    mScenesPagerFragment.setInitialSavedState(mSavedPagerState);
  //  }
  //  fragmentTransaction.commit();
  //}

  //@Override public void onHidden() {
  //  super.onHidden();
  //  // Kills the scenes pager.
  //  if (mScenesPagerFragment != null) {
  //    // Saves the state if the fragment was already added.
  //    if (getFragmentManager().findFragmentById(R.id.theater_scenesPagerLayout) != null) {
  //      mSavedPagerState = getFragmentManager().saveFragmentInstanceState(mScenesPagerFragment);
  //    }
  //    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
  //    fragmentTransaction.remove(mScenesPagerFragment);
  //    fragmentTransaction.commit();
  //  }
  //}

  //@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
  //  super.onViewCreated(view, savedInstanceState);
  //  // Initializes the Theater API
  //  mTheaterApi = NetworkUtil.createApi(TheaterApi.class);
  //}

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initializes the Theater API
    mTheaterApi = NetworkUtil.createApi(TheaterApi.class);
    ScenesPagerFragment scenesPagerFragment =
        (ScenesPagerFragment) getSupportFragmentManager().findFragmentById(R.id.scenesPager);
    scenesPagerFragment.setVisibilityListener(this);
    scenesPagerFragment.setScenesFetcher(this);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_theater, this);
  }

  @Override public void onResume() {
    super.onResume();
    navigateToTheaterInternal();
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mTheaterApi.fetchScenes(AppContainer.getAuthManager().getCurrentUser());
  }

  //@Override public void onClickEvent(MotionEvent e) {
  //  if (mScenesPagerFragment != null) {
  //    mScenesPagerFragment.onClickEvent(e);
  //  }
  //}
}


