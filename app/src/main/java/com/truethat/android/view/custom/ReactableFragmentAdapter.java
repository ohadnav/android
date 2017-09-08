package com.truethat.android.view.custom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.ReactableFragment;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class ReactableFragmentAdapter extends BaseFragmentAdapter<Reactable> {

  public ReactableFragmentAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.DISPLAYED_REACTABLE.name(), mItems.get(position).toString());
    }
    return ReactableFragment.newInstance(mItems.get(position));
  }
}
