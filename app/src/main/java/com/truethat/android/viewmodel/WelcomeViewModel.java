package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import com.truethat.android.viewmodel.viewinterface.WelcomeViewInterface;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

public class WelcomeViewModel extends BaseViewModel<WelcomeViewInterface> {
  public final ObservableBoolean mErrorTextVisibility = new ObservableBoolean(false);
}
