package com.truethat.android.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Proudly created by ohad on 09/10/2017 for TrueThat.
 */

public class ClickableViewPager extends FragmentViewPager {
  /**
   * Invoked on clicks on the pager. For example, used to navigate scenes.
   */
  private ClickEventListener mClickListener;

  public ClickableViewPager(Context context) {
    this(context, null);
  }

  public ClickableViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    initTapGesture();
  }

  public void setClickListener(@Nullable ClickEventListener clickListener) {
    mClickListener = clickListener;
  }

  private void initTapGesture() {
    // Used to enable detection of swipes and clicks.
    final GestureDetector tapGestureDetector =
        new GestureDetector(getContext(), new TapGestureListener());

    setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        tapGestureDetector.onTouchEvent(event);
        return false;
      }
    });
  }

  public interface ClickEventListener {
    void onClickEvent(MotionEvent e);
  }

  private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override public boolean onSingleTapConfirmed(MotionEvent e) {
      if (mClickListener != null) {
        mClickListener.onClickEvent(e);
        return true;
      }
      return false;
    }
  }
}
