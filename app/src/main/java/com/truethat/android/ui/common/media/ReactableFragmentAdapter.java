package com.truethat.android.ui.common.media;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import com.truethat.android.model.Reactable;
import com.truethat.android.ui.activity.TheaterActivity;
import java.util.LinkedList;
import java.util.List;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 * <p>
 * {@link PagerAdapter} for {@link ReactableFragment}, so that {@link Reactable} are displayed
 * nicely in {@link TheaterActivity}.
 */

public class ReactableFragmentAdapter extends FragmentStatePagerAdapter {
  private List<Reactable> mReactables = new LinkedList<>();

  public ReactableFragmentAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    return mReactables.get(position).createFragment();
  }

  @Override public int getCount() {
    return mReactables.size();
  }

  /**
   * Appends {@code reactables} to {@link #mReactables}, so that they will be displayed at the end.
   * @param reactables new {@link Reactable} fetched from our backend.
   */
  public void append(List<Reactable> reactables) {
    mReactables.addAll(reactables);
    notifyDataSetChanged();
  }
}
