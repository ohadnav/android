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
import com.truethat.android.common.network.RepertoireApi;
import com.truethat.android.databinding.FragmentRepertoireBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.custom.ClickableViewPager;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

public class RepertoireFragment extends
    MainFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentRepertoireBinding>
    implements ScenesPagerFragment.ScenesFetcher, ClickableViewPager.ClickEventListener {
  private ScenesPagerFragment mScenesPagerFragment;
  private SavedState mSavedPagerState;
  /**
   * API interface for getting scenes.
   */
  private RepertoireApi mRepertoireApi;

  public static RepertoireFragment newInstance() {
    Log.d(RepertoireFragment.class.getSimpleName(), "newInstance");
    Bundle args = new Bundle();
    RepertoireFragment fragment = new RepertoireFragment();
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
    fragmentTransaction.replace(R.id.repertoire_scenesPagerLayout, mScenesPagerFragment);
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
      if (getFragmentManager().findFragmentById(R.id.repertoire_scenesPagerLayout) != null) {
        mSavedPagerState = getFragmentManager().saveFragmentInstanceState(mScenesPagerFragment);
      }
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.remove(mScenesPagerFragment);
      fragmentTransaction.commit();
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Initializes the Repertoire API
    mRepertoireApi = NetworkUtil.createApi(RepertoireApi.class);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_repertoire, getContext());
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mRepertoireApi.fetchScenes(AppContainer.getAuthManager().getCurrentUser());
  }

  @Override public void onClickEvent(MotionEvent e) {
    if (mScenesPagerFragment != null) {
      mScenesPagerFragment.onClickEvent(e);
    }
  }
}
