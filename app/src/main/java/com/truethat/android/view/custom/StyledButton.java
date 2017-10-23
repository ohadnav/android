package com.truethat.android.view.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import com.truethat.android.common.util.StyleUtil;

/**
 * Proudly created by ohad on 23/10/2017 for TrueThat.
 */

public class StyledButton extends AppCompatButton {
  public StyledButton(Context context) {
    super(context);
    setTypeface(StyleUtil.getCustomFont(context, null));
  }

  public StyledButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    setTypeface(StyleUtil.getCustomFont(context, attrs));
  }

  public StyledButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setTypeface(StyleUtil.getCustomFont(context, attrs));
  }
}
