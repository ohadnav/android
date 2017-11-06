package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.AuthListener;

/**
 * Proudly created by ohad on 22/10/2017 for TrueThat.
 */

public class SplashActivity extends AppCompatActivity implements AuthListener {
  /**
   * Logging tag.
   */
  String TAG = this.getClass().getSimpleName();

  @Override public void onAuthOk() {
    Log.d(TAG, "onAuthOk");
    // Start main activity
    startActivity(new Intent(SplashActivity.this, StudioActivity.class));
    // close splash activity
    finish();
  }

  @Override public void onAuthFailed() {
    Log.d(TAG, "onAuthFailed");
    // Start welcome activity
    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
    // close splash activity
    finish();
  }

  @Override public String getTAG() {
    return TAG;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //AppContainer.getAuthManager().auth(this);
    AppContainer.getAuthManager().signIn(this);
  }
}