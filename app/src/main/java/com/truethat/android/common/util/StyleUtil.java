package com.truethat.android.common.util;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import com.truethat.android.R;
import java.util.Objects;

/**
 * Proudly created by ohad on 23/10/2017 for TrueThat.
 */

public class StyleUtil {
  public static final float DEFAULT_SCALE = 0.5f;
  private static final long ANIMATION_DURATION = 150;
  private static final float BOUNCE_BIG_SCALE = 0.8f;
  private static final float BOUNCE_SMALL_SCALE = 0.3f;
  private static final float BOUNCE_TINY_SCALE = 0.1f;
  private static final String FONT_NAME = "truethat";
  private static final String ATTRIBUTE_NAMESPACE = "http://schemas.android.com/apk/res/android";

  /**
   * @param context of the view
   * @param attrs   of the view
   *
   * @return the localized and customized font to be used.
   */
  public static Typeface getCustomFont(Context context, @Nullable AttributeSet attrs) {
    boolean bold =
        attrs != null && Objects.equals(attrs.getAttributeValue(ATTRIBUTE_NAMESPACE, "textStyle"),
            "bold");
    String assetSuffix = (bold ? "-bold" : "-regular") + ".ttf";
    String assetName = FONT_NAME + assetSuffix;
    Typeface typeface = Typeface.createFromAsset(context.getAssets(), assetName);
    return Typeface.create(typeface, bold ? Typeface.BOLD : Typeface.NORMAL);
  }

  /**
   * Sets rounded corners and a gradient background.
   *
   * @param view to apply.
   */
  public static void setRoundedCorners(View view) {
    GradientDrawable gradientDrawable =
        new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[] {
            ResourcesCompat.getColor(view.getResources(), R.color.primary,
                view.getContext().getTheme()),
            ResourcesCompat.getColor(view.getResources(), R.color.primary,
                view.getContext().getTheme()),
            ResourcesCompat.getColor(view.getResources(), R.color.secondary,
                view.getContext().getTheme())
        });
    gradientDrawable.setCornerRadius(Math.min(view.getHeight(), view.getWidth()) / 2f);

    view.setBackground(gradientDrawable);
  }

  /**
   * Bounce in animation.
   *
   * @param view      to animate.
   * @param endAction to run once the entire animation is finished successfully.
   */
  @MainThread public static void bounceIn(final View view, @Nullable final Runnable endAction) {
    view.animate()
        .alpha(0.7f)
        .scaleX(BOUNCE_SMALL_SCALE)
        .scaleY(BOUNCE_SMALL_SCALE)
        .setDuration(ANIMATION_DURATION)
        .setListener(new Animator.AnimatorListener() {
          @Override public void onAnimationStart(Animator animation) {
            view.animate()
                .alpha(1f)
                .scaleX(BOUNCE_BIG_SCALE)
                .scaleY(BOUNCE_BIG_SCALE)
                .setDuration(ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                  @Override public void onAnimationStart(Animator animation) {
                    view.animate()
                        .scaleX(DEFAULT_SCALE)
                        .scaleY(DEFAULT_SCALE)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(new Animator.AnimatorListener() {
                          @Override public void onAnimationStart(Animator animation) {

                          }

                          @Override public void onAnimationEnd(Animator animation) {
                            if (endAction != null) {
                              endAction.run();
                            }
                          }

                          @Override public void onAnimationCancel(Animator animation) {
                            setDefaultScale(view, 1);
                          }

                          @Override public void onAnimationRepeat(Animator animation) {

                          }
                        })
                        .start();
                  }

                  @Override public void onAnimationEnd(Animator animation) {

                  }

                  @Override public void onAnimationCancel(Animator animation) {
                    setDefaultScale(view, 1);
                  }

                  @Override public void onAnimationRepeat(Animator animation) {

                  }
                })
                .start();
          }

          @Override public void onAnimationEnd(Animator animation) {

          }

          @Override public void onAnimationCancel(Animator animation) {
            setDefaultScale(view, 1);
          }

          @Override public void onAnimationRepeat(Animator animation) {

          }
        })
        .start();
  }

  /**
   * Bounce out animation.
   *
   * @param view      to animate.
   * @param endAction to run once the entire animation is finished successfully.
   */
  @MainThread public static void bounceOut(final View view, @Nullable final Runnable endAction) {
    view.animate()
        .alpha(1f)
        .scaleX(BOUNCE_SMALL_SCALE)
        .scaleY(BOUNCE_SMALL_SCALE)
        .setDuration(ANIMATION_DURATION)
        .setListener(new Animator.AnimatorListener() {
          @Override public void onAnimationStart(Animator animation) {
            view.animate()
                .scaleX(BOUNCE_BIG_SCALE)
                .scaleY(BOUNCE_BIG_SCALE)
                .setDuration(ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                  @Override public void onAnimationStart(Animator animation) {
                    view.animate()
                        .alpha(0)
                        .scaleX(BOUNCE_TINY_SCALE)
                        .scaleY(BOUNCE_TINY_SCALE)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(new Animator.AnimatorListener() {
                          @Override public void onAnimationStart(Animator animation) {

                          }

                          @Override public void onAnimationEnd(Animator animation) {
                            if (endAction != null) {
                              endAction.run();
                            }
                          }

                          @Override public void onAnimationCancel(Animator animation) {
                            setDefaultScale(view, 0);
                          }

                          @Override public void onAnimationRepeat(Animator animation) {

                          }
                        })
                        .start();
                  }

                  @Override public void onAnimationEnd(Animator animation) {

                  }

                  @Override public void onAnimationCancel(Animator animation) {
                    setDefaultScale(view, 0);
                  }

                  @Override public void onAnimationRepeat(Animator animation) {

                  }
                })
                .start();
          }

          @Override public void onAnimationEnd(Animator animation) {

          }

          @Override public void onAnimationCancel(Animator animation) {
            setDefaultScale(view, 0);
          }

          @Override public void onAnimationRepeat(Animator animation) {

          }
        })
        .start();
  }

  private static void setDefaultScale(View view, float alpha) {
    view.setScaleX(DEFAULT_SCALE);
    view.setScaleY(DEFAULT_SCALE);
    view.setAlpha(alpha);
  }
}
