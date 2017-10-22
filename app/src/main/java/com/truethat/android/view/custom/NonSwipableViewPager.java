package com.truethat.android.view.custom;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import java.lang.reflect.Field;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

public class NonSwipableViewPager extends FragmentViewPager {
  private static final int SCROLL_DURATION = 350;

  public NonSwipableViewPager(Context context) {
    super(context);
    initScroller();
  }

  public NonSwipableViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    initScroller();
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }

  //down one is added for smooth scrolling

  private void initScroller() {
    try {
      Class<?> viewpager = ViewPager.class;
      Field scroller = viewpager.getDeclaredField("mScroller");
      scroller.setAccessible(true);
      scroller.set(this, new UnifiedScroller(getContext()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class UnifiedScroller extends Scroller {
    UnifiedScroller(Context context) {
      super(context, new DecelerateInterpolator());
    }

    @Override public void startScroll(int startX, int startY, int dx, int dy, int duration) {
      super.startScroll(startX, startY, dx, dy, SCROLL_DURATION);
    }
  }
}
