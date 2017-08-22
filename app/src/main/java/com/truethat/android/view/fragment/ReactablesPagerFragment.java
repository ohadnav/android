package com.truethat.android.view.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.FragmentReactablesPagerBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.custom.ReactableFragmentAdapter;
import com.truethat.android.viewmodel.ReactablesPagerViewModel;
import com.truethat.android.viewmodel.viewinterface.ReactablesPagerViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public class ReactablesPagerFragment extends
    BaseFragment<ReactablesPagerViewInterface, ReactablesPagerViewModel, FragmentReactablesPagerBinding>
    implements ReactablesPagerViewInterface {
  private static final String ARG_DETECT_REACTIONS = "detectReactions";
  @BindView(R.id.reactablesPager) ViewPager mPager;
  private boolean mDetectReactions = false;
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private ReactablePagerListener mListener;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof ReactablePagerListener) {
      mListener = (ReactablePagerListener) context;
    } else {
      throw new RuntimeException(context.getClass().getSimpleName()
          + " must implement "
          + ReactablePagerListener.class.getSimpleName());
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    // Navigation between reactables and activities.
    mRootView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().next();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    mPager.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().next();
      }

      @Override public void onSwipeRight() {
        getViewModel().previous();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    // Initialize views
    mReactableFragmentAdapter =
        new ReactableFragmentAdapter(getActivity().getSupportFragmentManager());
    mPager.setAdapter(mReactableFragmentAdapter);
    // Initializes view model parameters.
    getViewModel().setDetectReactions(mDetectReactions);
    return mRootView;
  }

  @Override public void onStart() {
    super.onStart();
    if (mDetectReactions) {
      AppContainer.getReactionDetectionManager().start(getBaseActivity());
    }
  }

  @Override public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
    super.onInflate(context, attrs, savedInstanceState);
    TypedArray styledAttributes =
        context.obtainStyledAttributes(attrs, R.styleable.ReactablesPagerFragment);
    // Saved state trumps XML.
    if (savedInstanceState == null || !savedInstanceState.getBoolean(ARG_DETECT_REACTIONS)) {
      mDetectReactions =
          styledAttributes.getBoolean(R.styleable.ReactablesPagerFragment_detect_reactions, false);
    }
    styledAttributes.recycle();
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      mDetectReactions = savedInstanceState.getBoolean(ARG_DETECT_REACTIONS);
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(ARG_DETECT_REACTIONS, mDetectReactions);
  }

  @VisibleForTesting public ReactableFragment getDisplayedReactable() {
    return (ReactableFragment) mReactableFragmentAdapter.instantiateItem(mPager,
        mPager.getCurrentItem());
  }

  @Override public void displayItem(int index) {
    mPager.setCurrentItem(index, true);
  }

  @Override public Call<List<Reactable>> buildFetchReactablesCall() {
    return mListener.buildFetchReactablesCall();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_reactables_pager, getContext());
  }

  public interface ReactablePagerListener {
    void onSwipeUp();

    void onSwipeDown();

    Call<List<Reactable>> buildFetchReactablesCall();
  }
}
