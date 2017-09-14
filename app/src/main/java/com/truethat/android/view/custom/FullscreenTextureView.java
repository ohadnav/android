package com.truethat.android.view.custom;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import com.truethat.android.common.util.AppUtil;

/**
 * A fullscreen {@link TextureView} that can be adjusted to a specified aspect ratio, while center
 * cropping its content.
 */
public class FullscreenTextureView extends TextureView {

  private int mRatioWidth = 0;
  private int mRatioHeight = 0;

  public FullscreenTextureView(Context context) {
    this(context, null);
  }

  public FullscreenTextureView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FullscreenTextureView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
   * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
   *
   * @param width  Relative horizontal size
   * @param height Relative vertical size
   */
  @MainThread public void setAspectRatio(int width, int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Size cannot be negative.");
    }
    mRatioWidth = width;
    mRatioHeight = height;
    requestLayout();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int newWidth = width;
    int newHeight = height;
    Point realDisplaySize = AppUtil.realDisplaySize(getContext());
    if (0 < mRatioWidth && 0 < mRatioHeight) {
      if (width < height * mRatioWidth / mRatioHeight) {
        newHeight = realDisplaySize.y;
        newWidth = width * realDisplaySize.y / (width * mRatioHeight / mRatioWidth);
      } else {
        newWidth = realDisplaySize.x;
        newHeight = height * realDisplaySize.x / (height * mRatioWidth / mRatioHeight);
      }
    }
    setMeasuredDimension(newWidth, newHeight);
  }
}
