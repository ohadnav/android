package com.truethat.android.view.custom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.truethat.android.model.Reactable;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class ReactableFragmentAdapter extends BaseFragmentAdapter<Reactable> {

  public ReactableFragmentAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    return mItems.get(position).createFragment();
  }
}
