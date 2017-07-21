package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import com.truethat.android.R;
import com.truethat.android.common.network.RepertoireApi;
import com.truethat.android.databinding.ActivityRepertoireBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.RepertoireViewModel;
import com.truethat.android.viewmodel.viewinterface.RepertoireViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

public class RepertoireActivity extends
    ReactablesPagerActivity<RepertoireViewInterface, RepertoireViewModel, ActivityRepertoireBinding> {
  public static final String FROM_REPERTOIRE = "fromRepertoire";
  /**
   * API interface for getting reactables.
   */
  private RepertoireApi mRepertoireApi;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_top, R.animator.slide_out_top);
    // Initialize API
    mRepertoireApi = createApiInterface(RepertoireApi.class);
  }

  @Override protected Call<List<Reactable>> buildFetchReactablesCall() {
    return mRepertoireApi.fetchRepertoire(mAuthManager.currentUser());
  }

  @Override protected void onSwipeDown() {
    Intent studioIntent = new Intent(RepertoireActivity.this, StudioActivity.class);
    studioIntent.putExtra(FROM_REPERTOIRE, true);
    startActivity(studioIntent);
  }

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_repertoire, this);
  }
}
