package com.truethat.android.view.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */

public class BaseDialog extends Dialog {

  public BaseDialog(@NonNull Context context, @StringRes int titleStringResourceId,
      @StringRes int messageStringResourceId, @StringRes int buttonStringResourceId) {
    super(context, android.R.style.Theme_Dialog);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog);
    setCanceledOnTouchOutside(true);
    // Init sub views
    StyledButton button = findViewById(R.id.dialog_button);
    StyledTextView title = findViewById(R.id.dialog_title);
    StyledTextView message = findViewById(R.id.dialog_message);
    // Set views text
    button.setText(buttonStringResourceId);
    title.setText(titleStringResourceId);
    message.setText(messageStringResourceId);
    // Set button to dismiss dialog.
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dismiss();
      }
    });
    if (getWindow() != null) {
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
  }
}
