package com.truethat.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.RepertoireApi;
import com.truethat.android.model.Reactable;
import java.util.List;
import retrofit2.Call;

public class RepertoireActivity extends ReactablesPagerActivity {
  public static final String FROM_REPERTOIRE = "fromRepertoire";
  /**
   * API interface for getting reactables.
   */
  private RepertoireApi mRepertoireApi;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_top, R.animator.slide_out_top);
    // Initialize API
    mRepertoireApi = NetworkUtil.createAPI(RepertoireApi.class);
  }

  @Override protected Call<List<Reactable>> buildFetchReactablesCall() {
    return mRepertoireApi.fetchRepertoire(App.getAuthModule().getUser());
  }

  @Override protected void onSwipeDown() {
    Intent studioIntent = new Intent(RepertoireActivity.this, StudioActivity.class);
    studioIntent.putExtra(FROM_REPERTOIRE, true);
    startActivity(studioIntent);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_repertoire;
  }
}
