package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
 * Theater is where users interact with poses.
 */
public class TheaterActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTheaterBinding>
    implements ScenesPagerFragment.ScenePagerListener {
  private TheaterApi mTheaterApi;
  private ScenesPagerFragment mPagerFragment;

  @Override public void onAuthOk() {
    super.onAuthOk();
    mPagerFragment.getViewModel().fetchScenes();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_theater, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_bottom, R.animator.slide_out_bottom);
    // Hooks the camera fragment
    mPagerFragment = (ScenesPagerFragment) getSupportFragmentManager().findFragmentById(
        R.id.scenesPagerFragment);
    // Initializes the Theater API
    mTheaterApi = NetworkUtil.createApi(TheaterApi.class);
  }

  @Override public void onSwipeUp() {
    startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
  }

  @Override public void onSwipeDown() {
  }

  @Override public Call<List<Scene>> buildFetchScenesCall() {
    return mTheaterApi.fetchScenes(AppContainer.getAuthManager().getCurrentUser());
  }
}


