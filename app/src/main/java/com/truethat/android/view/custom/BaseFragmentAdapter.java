package com.truethat.android.view.custom;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import eu.inloop.viewmodel.support.ViewModelStatePagerAdapter;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

abstract class BaseFragmentAdapter extends ViewModelStatePagerAdapter {
  FragmentViewPager mFragmentViewPager;

  BaseFragmentAdapter(@NonNull FragmentManager fm, FragmentViewPager fragmentViewPager) {
    super(fm);
    mFragmentViewPager = fragmentViewPager;
  }
}
