package com.truethat.android.view.custom;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import com.truethat.android.view.fragment.BaseFragment;
import com.truethat.android.view.fragment.VisibilityListener;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

public class FragmentViewPager extends ViewPager implements VisibilityListener {
  private int mLastPosition = -1;
  private VisibilityListener mVisibilityListener;

  public FragmentViewPager(Context context) {
    super(context);
    addOnPageChangeListener(new OnPageChangeListener());
  }

  public FragmentViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    addOnPageChangeListener(new OnPageChangeListener());
  }

  public void setVisibilityListener(VisibilityListener visibilityListener) {
    mVisibilityListener = visibilityListener;
  }

  @Override public boolean shouldBeVisible(Object o) {
    return getAdapter() != null
        && getAdapter().getCount() != 0
        && o.equals(getAdapter().instantiateItem(this, getCurrentItem()))
        && mVisibilityListener != null
        && mVisibilityListener.shouldBeVisible(this);
  }

  private class OnPageChangeListener implements ViewPager.OnPageChangeListener {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @CallSuper @Override public void onPageSelected(int position) {
      if (getAdapter() != null && mLastPosition != position && mLastPosition >= 0) {
        ((BaseFragment) getAdapter().instantiateItem(FragmentViewPager.this,
            mLastPosition)).setUserVisibleHint(false);
      }
      ((BaseFragment) getAdapter().instantiateItem(FragmentViewPager.this,
          position)).setUserVisibleHint(true);
      mLastPosition = position;
    }

    @Override public void onPageScrollStateChanged(int state) {

    }
  }
}
