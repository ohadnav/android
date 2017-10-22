package com.truethat.android.view.custom;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.SceneFragment;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class SceneFragmentAdapter extends FragmentObservableListAdapter<Scene> {

  public SceneFragmentAdapter(@NonNull FragmentManager fm,
      @NonNull FragmentViewPager fragmentViewPager) {
    super(fm, fragmentViewPager);
  }

  @Override public Fragment getItem(int position) {
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.DISPLAYED_SCENE.name(), mItems.get(position).toString());
    }
    SceneFragment sceneFragment = SceneFragment.newInstance(mItems.get(position));
    sceneFragment.setVisibilityListener(mFragmentViewPager);
    return sceneFragment;
  }
}
