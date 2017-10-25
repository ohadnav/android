package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.ActivityOnBoardingBinding;
import com.truethat.android.view.custom.NonSwipableViewPager;
import com.truethat.android.view.fragment.BaseFragment;
import com.truethat.android.view.fragment.OnBoardingHiStageFragment;
import com.truethat.android.view.fragment.OnBoardingSignUpStageFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import com.truethat.android.viewmodel.viewinterface.OnBoardingListener;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import eu.inloop.viewmodel.support.ViewModelStatePagerAdapter;

public class OnBoardingActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityOnBoardingBinding>
    implements OnBoardingListener {
  @VisibleForTesting public static final int HI_STAGE_INDEX = 0;
  @VisibleForTesting public static final int SIGN_UP_STAGE_INDEX = 1;
  private static final String BUNDLE_STAGE_INDEX = "stageIndex";
  private static final int FIRST_STAGE_INDEX = HI_STAGE_INDEX;
  private static final int STAGE_COUNT = 2;
  @VisibleForTesting public OnBoardingHiStageFragment mHiStageFragment;
  @VisibleForTesting public OnBoardingSignUpStageFragment mSignUpStageFragment;
  @BindView(R.id.onBoarding_mainPager) NonSwipableViewPager mMainPager;
  private int mStageIndex = FIRST_STAGE_INDEX;

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_on_boarding, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Restores last stage index
    if (savedInstanceState != null) {
      mStageIndex = savedInstanceState.getInt(BUNDLE_STAGE_INDEX, FIRST_STAGE_INDEX);
    }
    ViewModelStatePagerAdapter pagerAdapter =
        new ViewModelStatePagerAdapter(getSupportFragmentManager()) {
          @Override public Fragment getItem(int position) {
            switch (position) {
              case HI_STAGE_INDEX:
                if (mHiStageFragment == null) {
                  mHiStageFragment = OnBoardingHiStageFragment.newInstance();
                  mHiStageFragment.setVisibilityListener(mMainPager);
                }
                return mHiStageFragment;
              case SIGN_UP_STAGE_INDEX:
                if (mSignUpStageFragment == null) {
                  mSignUpStageFragment = OnBoardingSignUpStageFragment.newInstance();
                  mSignUpStageFragment.setVisibilityListener(mMainPager);
                }
                return mSignUpStageFragment;
              default:
                throw new IllegalArgumentException("Illegal position.");
            }
          }

          @Override public Object instantiateItem(ViewGroup container, int position) {
            BaseFragment fragment = (BaseFragment) super.instantiateItem(container, position);
            switch (position) {
              case HI_STAGE_INDEX:
                mHiStageFragment = (OnBoardingHiStageFragment) fragment;
                break;
              case SIGN_UP_STAGE_INDEX:
                mSignUpStageFragment = (OnBoardingSignUpStageFragment) fragment;
                break;
            }
            if (fragment.getVisibilityListener() == null) {
              fragment.setVisibilityListener(mMainPager);
            }
            return fragment;
          }

          @Override public int getCount() {
            return STAGE_COUNT;
          }
        };
    mMainPager.setAdapter(pagerAdapter);
    mMainPager.setVisibilityListener(this);
  }

  @Override public void onStop() {
    super.onStop();
    // Destroy all items, so that they are properly restored upon resume
    if (mHiStageFragment != null) {
      mMainPager.getAdapter().destroyItem(mMainPager, HI_STAGE_INDEX, mHiStageFragment);
      mHiStageFragment = null;
    }
    if (mSignUpStageFragment != null) {
      mMainPager.getAdapter().destroyItem(mMainPager, SIGN_UP_STAGE_INDEX, mSignUpStageFragment);
      mSignUpStageFragment = null;
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(BUNDLE_STAGE_INDEX, mStageIndex);
  }

  @Override public void onResume() {
    super.onResume();
    // Maybe we are here by mistake.
    if (AppContainer.getAuthManager().isAuthOk()) {
      finish();
    } else {
      mMainPager.setCurrentItem(mStageIndex);
    }
  }

  public int getStageIndex() {
    return mStageIndex;
  }

  public void nextStage() {
    if (mStageIndex < STAGE_COUNT - 1) {
      mStageIndex++;
      mMainPager.setCurrentItem(mStageIndex);
    } else {
      // On boarding is complete
      startActivity(new Intent(this, MainActivity.class));
      finish();
    }
  }
}
