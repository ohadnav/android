package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.RepertoireApi;
import com.truethat.android.databinding.ActivityRepertoireBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.ReactablesPagerFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

public class RepertoireActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityRepertoireBinding>
    implements ReactablesPagerFragment.ReactablePagerListener {
  public static final String FROM_REPERTOIRE = "fromRepertoire";
  private ReactablesPagerFragment mPagerFragment;
  /**
   * API interface for getting reactables.
   */
  private RepertoireApi mRepertoireApi;

  @Override public void onAuthOk() {
    super.onAuthOk();
    mPagerFragment.getViewModel().fetchReactables();
  }

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_repertoire, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_top, R.animator.slide_out_top);
    mPagerFragment = (ReactablesPagerFragment) getSupportFragmentManager().findFragmentById(
        R.id.reactablesPagerFragment);
    // Initialize API
    mRepertoireApi = NetworkUtil.createApi(RepertoireApi.class);
  }

  @Override public void onSwipeUp() {

  }

  @Override public void onSwipeDown() {
    Intent studioIntent = new Intent(RepertoireActivity.this, StudioActivity.class);
    studioIntent.putExtra(FROM_REPERTOIRE, true);
    startActivity(studioIntent);
  }

  @Override public Call<List<Reactable>> buildFetchReactablesCall() {
    return mRepertoireApi.fetchRepertoire(AppContainer.getAuthManager().getCurrentUser());
  }
}
