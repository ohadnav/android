package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.common.util.AppUtil;
import com.truethat.android.databinding.ActivityMainBinding;
import com.truethat.android.view.custom.ClickableViewPager;
import com.truethat.android.view.fragment.MainFragment;
import com.truethat.android.view.fragment.RepertoireFragment;
import com.truethat.android.view.fragment.StudioFragment;
import com.truethat.android.view.fragment.TheaterFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import com.truethat.android.viewmodel.viewinterface.ToolbarViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import eu.inloop.viewmodel.support.ViewModelStatePagerAdapter;

/**
 * Proudly created by ohad on 09/10/2017 for TrueThat.
 */

public class MainActivity
    extends BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityMainBinding>
    implements AuthListener, BaseViewInterface, ToolbarViewInterface {
  @VisibleForTesting public static final int TOOLBAR_THEATER_INDEX = 2;
  @VisibleForTesting public static final int TOOLBAR_STUDIO_INDEX = 1;
  @VisibleForTesting public static final int TOOLBAR_REPERTOIRE_INDEX = 0;
  @VisibleForTesting public static final float TOOLBAR_LATERAL_SELECTED_SCALE = 0.5f;
  @VisibleForTesting public static final float TOOLBAR_STUDIO_SELECTED_SCALE = 0.66f;
  @VisibleForTesting public static final float TOOLBAR_LATERAL_DESELECTED_SCALE = 0.33f;
  @VisibleForTesting public static final float TOOLBAR_STUDIO_DESELECTED_SCALE = 0.33f;
  @VisibleForTesting public static final float TOOLBAR_STUDIO_SELECTED_TRANSLATION_Y = -50f;
  @VisibleForTesting public static final float TOOLBAR_LATERAL_SELECTED_TRANSLATION_Y = 0f;
  public static final float TOOLBAR_DESELECTED_TRANSLATION_Y = 80f;
  private static final String BUNDLE_CURRENT_MAIN_FRAGMENT_INDEX = "currentMainFragmentIndex";
  private static final int TOOLBAR_ANIMATION_DURATION_MS = 150;
  private static final float TOOLBAR_HIDE_TRANSLATION = 100f;
  @VisibleForTesting public static int sLaunchIndex = TOOLBAR_STUDIO_INDEX;
  @VisibleForTesting public static float sToolbarLateralTranslationX = 100f;
  @VisibleForTesting @BindView(R.id.toolbar_layout) public ConstraintLayout mToolbarLayout;
  @VisibleForTesting @BindView(R.id.toolbar_repertoire) public ImageButton mToolbarRepertoire;
  @VisibleForTesting @BindView(R.id.toolbar_studio) public ImageButton mToolbarStudio;
  @VisibleForTesting @BindView(R.id.toolbar_theater) public ImageButton mToolbarTheater;
  @VisibleForTesting public StudioFragment mStudioFragment;
  @VisibleForTesting public TheaterFragment mTheaterFragment;
  @VisibleForTesting public RepertoireFragment mRepertoireFragment;
  @BindView(R.id.mainPager) ClickableViewPager mMainPager;
  private Integer mCurrentMainFragmentIndex;

  public ImageButton getToolbarRepertoire() {
    return mToolbarRepertoire;
  }

  public ImageButton getToolbarStudio() {
    return mToolbarStudio;
  }

  public ImageButton getToolbarTheater() {
    return mToolbarTheater;
  }

  @VisibleForTesting public StudioFragment getStudioFragment() {
    return mStudioFragment;
  }

  @VisibleForTesting public ClickableViewPager getMainPager() {
    return mMainPager;
  }

  /**
   * Authentication success callback.
   */
  public void onAuthOk() {
    Log.d(TAG, "onAuthOk");
    if (mCurrentMainFragmentIndex == null) {
      Log.w(TAG, "main fragment index has not been initialized onCreate.");
      mCurrentMainFragmentIndex = sLaunchIndex;
    }
    mMainPager.setCurrentItem(mCurrentMainFragmentIndex);
  }

  /**
   * Authentication failure callback.
   */
  public void onAuthFailed() {
    Log.d(TAG, "onAuthFailed");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
      }
    });
  }

  @Override public void navigateToTheater() {
    mMainPager.setCurrentItem(TOOLBAR_THEATER_INDEX);
  }

  @Override public void navigateToStudio() {
    mMainPager.setCurrentItem(TOOLBAR_STUDIO_INDEX);
  }

  @Override public void navigateToRepertoire() {
    mMainPager.setCurrentItem(TOOLBAR_REPERTOIRE_INDEX);
  }

  @Override public void hideToolbar() {
    Log.d(TAG, "hideToolbar");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mToolbarLayout.animate().alpha(0).setDuration(TOOLBAR_ANIMATION_DURATION_MS).start();
        mToolbarLayout.animate()
            .translationY(TOOLBAR_HIDE_TRANSLATION)
            .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
            .start();
      }
    });
  }

  @Override public void showToolbar() {
    Log.d(TAG, "showToolbar");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mToolbarLayout.animate().alpha(1).setDuration(TOOLBAR_ANIMATION_DURATION_MS).start();
        mToolbarLayout.animate()
            .translationY(0f)
            .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
            .start();
      }
    });
  }

  @VisibleForTesting public MainFragment getCurrentMainFragment() {
    return (MainFragment) mMainPager.getAdapter()
        .instantiateItem(mMainPager, mMainPager.getCurrentItem());
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    // Restores last main index
    if (savedInstanceState != null) {
      mCurrentMainFragmentIndex =
          savedInstanceState.getInt(BUNDLE_CURRENT_MAIN_FRAGMENT_INDEX, sLaunchIndex);
    }
  }

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_main, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    defaultToolbarStyle();
    ViewModelStatePagerAdapter pagerAdapter =
        new ViewModelStatePagerAdapter(getSupportFragmentManager()) {
          @Override public Fragment getItem(int position) {
            switch (position) {
              case TOOLBAR_REPERTOIRE_INDEX:
                if (mRepertoireFragment == null) {
                  mRepertoireFragment = RepertoireFragment.newInstance();
                  mRepertoireFragment.setVisibilityListener(mMainPager);
                }
                return mRepertoireFragment;
              case TOOLBAR_STUDIO_INDEX:
                if (mStudioFragment == null) {
                  mStudioFragment = StudioFragment.newInstance();
                  mStudioFragment.setVisibilityListener(mMainPager);
                }
                return mStudioFragment;
              case TOOLBAR_THEATER_INDEX:
                if (mTheaterFragment == null) {
                  mTheaterFragment = TheaterFragment.newInstance();
                  mTheaterFragment.setVisibilityListener(mMainPager);
                }
                return mTheaterFragment;
              default:
                throw new IllegalArgumentException("Illegal position.");
            }
          }

          @Override public int getCount() {
            return 3;
          }
        };
    mMainPager.setAdapter(pagerAdapter);
    mMainPager.setVisibilityListener(this);

    mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO: smooth toolbar icons resizing on scrolling
      }

      @Override public void onPageSelected(int position) {
        mCurrentMainFragmentIndex = position;
        switch (position) {
          case TOOLBAR_REPERTOIRE_INDEX:
            navigateToRepertoireInternal();
            break;
          case TOOLBAR_STUDIO_INDEX:
            navigateToStudioInternal();
            break;
          case TOOLBAR_THEATER_INDEX:
            navigateToTheaterInternal();
            break;
        }
      }

      @Override public void onPageScrollStateChanged(int state) {

      }
    });

    // Brings toolbar to front
    mToolbarLayout.bringToFront();
    // Updates styling constants
    sToolbarLateralTranslationX = AppUtil.realDisplaySize(this).x / 10f;
  }

  @Override public boolean shouldBeVisible(Object o) {
    if (!super.shouldBeVisible(o)) {
      return false;
    }
    // View pager should not be visible until user is authenticated.
    return o != mMainPager || AppContainer.getAuthManager().isAuthOk();
  }

  @Override public void onResume() {
    super.onResume();
    AppContainer.getAuthManager().auth(this);
  }

  @OnClick({ R.id.toolbar_repertoire, R.id.toolbar_theater }) void toolbarClickNavigation(View v) {
    switch (v.getId()) {
      case R.id.toolbar_repertoire:
        navigateToRepertoire();
        break;
      case R.id.toolbar_theater:
        navigateToTheater();
        break;
      case R.id.toolbar_studio:
        navigateToStudio();
        break;
      default:
        mMainPager.setCurrentItem(sLaunchIndex);
    }
  }

  private void navigateToTheaterInternal() {
    Log.d(TAG, "navigateToTheater");
    defaultToolbarStyle();
    // Scales theater toolbar icon
    mToolbarTheater.animate()
        .scaleX(TOOLBAR_LATERAL_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarTheater.animate()
        .scaleY(TOOLBAR_LATERAL_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Raises the icon
    mToolbarTheater.animate()
        .translationY(TOOLBAR_LATERAL_SELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    resetStudioToolbarTouchListeners();
    // Set main pager click listener to the relevant scene pager
    mMainPager.setClickListener(mTheaterFragment);
  }

  private void navigateToStudioInternal() {
    Log.d(TAG, "navigateToStudio");
    defaultToolbarStyle();
    // Scales studio toolbar icon
    mToolbarStudio.animate()
        .scaleX(TOOLBAR_STUDIO_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarStudio.animate()
        .scaleY(TOOLBAR_STUDIO_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Raises the studio icon
    mToolbarStudio.animate()
        .translationY(TOOLBAR_STUDIO_SELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Moves theater and repertoire further away from studio capture button
    mToolbarTheater.animate().translationX(0f).setDuration(TOOLBAR_ANIMATION_DURATION_MS).start();
    mToolbarRepertoire.animate()
        .translationX(0f)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Disable pager click listener
    mMainPager.setClickListener(null);
  }

  private void navigateToRepertoireInternal() {
    Log.d(TAG, "navigateToRepertoire");
    defaultToolbarStyle();
    // Scales repertoire toolbar icon
    mToolbarRepertoire.animate()
        .scaleX(TOOLBAR_LATERAL_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarRepertoire.animate()
        .scaleY(TOOLBAR_LATERAL_SELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Raises the icon
    mToolbarRepertoire.animate()
        .translationY(TOOLBAR_LATERAL_SELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    resetStudioToolbarTouchListeners();
    // Set main pager click listener to the relevant scene pager
    mMainPager.setClickListener(mRepertoireFragment);
  }

  private void resetStudioToolbarTouchListeners() {
    mToolbarStudio.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        toolbarClickNavigation(v);
      }
    });
    mToolbarStudio.setOnLongClickListener(null);
    mToolbarStudio.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return false;
      }
    });
  }

  private void defaultToolbarStyle() {
    // Scales buttons to smaller size
    mToolbarRepertoire.animate()
        .scaleX(TOOLBAR_LATERAL_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarRepertoire.animate()
        .scaleY(TOOLBAR_LATERAL_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarStudio.animate()
        .scaleX(TOOLBAR_STUDIO_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarStudio.animate()
        .scaleY(TOOLBAR_STUDIO_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarTheater.animate()
        .scaleX(TOOLBAR_LATERAL_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarTheater.animate()
        .scaleY(TOOLBAR_LATERAL_DESELECTED_SCALE)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Moves theater and repertoire closer to studio capture button
    mToolbarTheater.animate()
        .translationX(-sToolbarLateralTranslationX)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarRepertoire.animate()
        .translationX(sToolbarLateralTranslationX)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Lowers the studio icon
    mToolbarStudio.animate()
        .translationY(TOOLBAR_DESELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    // Lowers the lateral icons
    mToolbarTheater.animate()
        .translationY(TOOLBAR_DESELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
    mToolbarRepertoire.animate()
        .translationY(TOOLBAR_DESELECTED_TRANSLATION_Y)
        .setDuration(TOOLBAR_ANIMATION_DURATION_MS)
        .start();
  }
}
