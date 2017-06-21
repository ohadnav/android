package com.truethat.android.theater;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import com.truethat.android.common.media.Reactable;
import com.truethat.android.common.media.ReactableFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 *
 * {@link PagerAdapter} for {@link ReactableFragment}, so that {@link Reactable} are displayed
 * nicely in {@link TheaterActivity}.
 */

class ReactableFragmentAdapter extends FragmentStatePagerAdapter {
  private List<Reactable> mReactables = new ArrayList<>();

  ReactableFragmentAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    return mReactables.get(position).createFragment();
  }

  @Override public int getCount() {
    return mReactables.size();
  }

  /**
   * @param reactables new {@link Reactable} fetched from our backend.
   */
  void add(List<Reactable> reactables) {
    mReactables.addAll(reactables);
    notifyDataSetChanged();
  }
}
