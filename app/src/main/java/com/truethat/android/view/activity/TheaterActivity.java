package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.databinding.ActivityTheaterBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.ReactablesPagerFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTheaterBinding>
    implements ReactablesPagerFragment.ReactablePagerListener {
  private TheaterApi mTheaterApi;
  private ReactablesPagerFragment mPagerFragment;

  @Override public void onAuthOk() {
    super.onAuthOk();
    mPagerFragment.getViewModel().fetchReactables();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_theater, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_bottom, R.animator.slide_out_bottom);
    // Hooks the camera fragment
    mPagerFragment = (ReactablesPagerFragment) getSupportFragmentManager().findFragmentById(
        R.id.reactablesPagerFragment);
    // Initializes the Theater API
    mTheaterApi = NetworkUtil.createApi(TheaterApi.class);
  }

  @Override public void onSwipeUp() {
    startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
  }

  @Override public void onSwipeDown() {
  }

  @Override public Call<List<Reactable>> buildFetchReactablesCall() {
    return mTheaterApi.fetchReactables(AppContainer.getAuthManager().getCurrentUser());
  }
}


