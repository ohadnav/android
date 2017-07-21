package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.application.auth.AuthListener;
import eu.inloop.viewmodel.IView;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface BaseViewInterface extends AuthListener, IView {
  void toast(String text);
}
