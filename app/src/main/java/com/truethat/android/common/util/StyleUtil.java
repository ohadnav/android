package com.truethat.android.common.util;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
}
