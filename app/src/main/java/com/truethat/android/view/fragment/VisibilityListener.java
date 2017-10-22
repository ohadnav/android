package com.truethat.android.view.fragment;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 * <p>
 * Inquires about this fragment visibility. Normally this will be the fragment's parent or the
 * view pager that contains the fragment.
 */

public interface VisibilityListener {
  /**
   * @param o to inquire about.
   *
   * @return whether the object should be visible. For example we use this for enabling {@link
   * BaseFragment#onVisible()}.
   */
  boolean shouldBeVisible(Object o);
}
