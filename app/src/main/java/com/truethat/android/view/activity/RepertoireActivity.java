package com.truethat.android.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.RepertoireApi;
import com.truethat.android.databinding.ActivityRepertoireBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.ScenesPagerFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

public class RepertoireActivity extends
    MainActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityRepertoireBinding>
    implements ScenesPagerFragment.ScenesFetcher {
  //private ScenesPagerFragment mScenesPagerFragment;
  //private SavedState mSavedPagerState;
  /**
   * API interface for getting scenes.
   */
  private RepertoireApi mRepertoireApi;

  //public static RepertoireActivity newInstance() {
  //  Log.d(RepertoireActivity.class.getSimpleName(), "newInstance");
  //  Bundle args = new Bundle();
  //  RepertoireActivity fragment = new RepertoireActivity();
  //  fragment.setArguments(args);
  //  return fragment;
  //}
  //
  //@Override public void maybeChangeVisibilityState() {
  //  super.maybeChangeVisibilityState();
  //  if (mScenesPagerFragment != null) {
  //    mScenesPagerFragment.maybeChangeVisibilityState();
  //  }
  //}
  //
  //@Override public void onVisible() {
  //  super.onVisible();
  //  showToolbar();
  //  // Adds the scenes pager.
  //  FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
  //  mScenesPagerFragment = ScenesPagerFragment.newInstance();
  //  mScenesPagerFragment.setVisibilityListener(this);
  //  mScenesPagerFragment.setScenesFetcher(this);
  //  fragmentTransaction.replace(R.id.repertoire_scenesPagerLayout, mScenesPagerFragment);
  //  if (mSavedPagerState != null) {
  //    mScenesPagerFragment.setInitialSavedState(mSavedPagerState);
  //  }
  //  fragmentTransaction.commit();
  //}
  //
  //@Override public void onHidden() {
  //  super.onHidden();
  //  // Kills the scenes pager.
  //  if (mScenesPagerFragment != null) {
  //    // Saves the state if the fragment was already added.
  //    if (getFragmentManager().findFragmentById(R.id.repertoire_scenesPagerLayout) != null) {
  //      mSavedPagerState = getFragmentManager().saveFragmentInstanceState(mScenesPagerFragment);
  //    }
  //    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
  //    fragmentTransaction.remove(mScenesPagerFragment);
  //    fragmentTransaction.commit();
  //  }
  //}

  //@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
  //  super.onViewCreated(view, savedInstanceState);
  //  // Initializes the Repertoire API
  //  mRepertoireApi = NetworkUtil.createApi(RepertoireApi.class);
  //}

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initializes the Repertoire API
    mRepertoireApi = NetworkUtil.createApi(RepertoireApi.class);
    ScenesPagerFragment scenesPagerFragment =
        (ScenesPagerFragment) getSupportFragmentManager().findFragmentById(R.id.scenesPager);
    scenesPagerFragment.setVisibilityListener(this);
    scenesPagerFragment.setScenesFetcher(this);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_repertoire, this);
    //return new ViewModelBindingConfig(R.layout.activity_repertoire, getContext());
  }

  @Override public void onResume() {
    super.onResume();
    navigateToRepertoireInternal();
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mRepertoireApi.fetchScenes(AppContainer.getAuthManager().getCurrentUser());
  }

  //@Override public void onClickEvent(MotionEvent e) {
  //  if (mScenesPagerFragment != null) {
  //    mScenesPagerFragment.onClickEvent(e);
  //  }
  //}
}
