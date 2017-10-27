package com.truethat.android.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.widget.ImageView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.common.util.StyleUtil;
import com.truethat.android.databinding.FragmentOnBoardingCheeseBinding;
import com.truethat.android.view.activity.OnBoardingActivity;
import com.truethat.android.viewmodel.OnBoardingCheeseStageViewModel;
import com.truethat.android.viewmodel.viewinterface.OnBoardingCheeseStageViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */

public class OnBoardingCheeseStageFragment extends
    OnBoardingStageFragment<OnBoardingCheeseStageViewInterface, OnBoardingCheeseStageViewModel, FragmentOnBoardingCheeseBinding>
    implements OnBoardingCheeseStageViewInterface {
  private static final long ANIMATION_DURATION = 200;
  @BindView(R.id.onBoarding_cheeseLayout) ConstraintLayout mCheeseLayout;
  @BindView(R.id.onBoarding_lostFaceLayout) ConstraintLayout mLostFaceLayout;
  @BindView(R.id.onBoarding_blindEmoji) ImageView mBlindEmoji;
  @BindView(R.id.onBoarding_leftEmoji) ImageView mLeftHappyEmoji;
  @BindView(R.id.onBoarding_rightEmoji) ImageView mRightHappyEmoji;
  @BindView(R.id.onBoarding_middleEmoji) ImageView mMiddleHappyEmoji;

  public static OnBoardingCheeseStageFragment newInstance() {
    Bundle args = new Bundle();
    OnBoardingCheeseStageFragment fragment = new OnBoardingCheeseStageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_on_boarding_cheese, getContext());
  }

  @Override public void showLostFaceLayout() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        bounceHappyEmojisOut(new Runnable() {
          @Override public void run() {
            mCheeseLayout.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
            mLostFaceLayout.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(new Runnable() {
                  @Override public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                      @Override public void run() {
                        StyleUtil.bounceIn(mBlindEmoji, null);
                      }
                    });
                  }
                })
                .start();
          }
        });
      }
    });
  }

  @Override public void showCheeseLayout() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        if (mLostFaceLayout.getAlpha() > 0) {
          StyleUtil.bounceOut(mBlindEmoji, new Runnable() {
            @Override public void run() {
              mLostFaceLayout.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
              mCheeseLayout.animate()
                  .alpha(1f)
                  .setDuration(ANIMATION_DURATION)
                  .withEndAction(new Runnable() {
                    @Override public void run() {
                      bounceHappyEmojisIn(null);
                    }
                  })
                  .start();
            }
          });
        } else {
          mCheeseLayout.animate()
              .alpha(1f)
              .setDuration(ANIMATION_DURATION)
              .withEndAction(new Runnable() {
                @Override public void run() {
                  bounceHappyEmojisIn(null);
                }
              })
              .start();
        }
      }
    });
  }

  @Override public void finishStage() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        bounceHappyEmojisIn(new Runnable() {
          @Override public void run() {
            mOnBoardingListener.onComplete(OnBoardingActivity.CHEESE_STAGE_INDEX);
          }
        });
      }
    });
  }

  /**
   * Gradually exposes and animate the happy emojis on the screen.
   *
   * @param endAction to run once all the emojis had been bounced in.
   */
  private void bounceHappyEmojisIn(@Nullable final Runnable endAction) {
    StyleUtil.bounceIn(mLeftHappyEmoji, new Runnable() {
      @Override public void run() {
        StyleUtil.bounceIn(mMiddleHappyEmoji, new Runnable() {
          @Override public void run() {
            StyleUtil.bounceIn(mRightHappyEmoji, endAction);
          }
        });
      }
    });
  }

  /**
   * Gradually hides the happy emojis on the screen.
   *
   * @param endAction to run once all the emojis had been bounced out.
   */
  private void bounceHappyEmojisOut(final Runnable endAction) {
    StyleUtil.bounceOut(mRightHappyEmoji, new Runnable() {
      @Override public void run() {
        StyleUtil.bounceOut(mMiddleHappyEmoji, new Runnable() {
          @Override public void run() {
            StyleUtil.bounceOut(mLeftHappyEmoji, endAction);
          }
        });
      }
    });
  }
}
