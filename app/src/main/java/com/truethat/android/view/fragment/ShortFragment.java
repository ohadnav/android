package com.truethat.android.view.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnTouch;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Short;
import com.truethat.android.viewmodel.ReactableViewModel;
import java.io.File;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class ShortFragment
    extends ReactableFragment<Short, ReactableViewModel<Short>, FragmentReactableBinding> {
  @BindView(R.id.videoSurface) SurfaceView mVideoSurface;
  private MediaPlayer mMediaPlayer;

  public ShortFragment() {
    // Required empty public constructor
  }

  public static ShortFragment newInstance(Short shortReactable) {
    ShortFragment shortFragment = new ShortFragment();
    ReactableFragment.prepareInstance(shortFragment, shortReactable);
    shortFragment.mAutomaticViewBinding = false;
    return shortFragment;
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onVisible() {
    super.onVisible();
    if (getViewModel().isReady()) {
      mMediaPlayer.start();
    }
  }

  public MediaPlayer getMediaPlayer() {
    return mMediaPlayer;
  }

  /**
   * Displays the video from {@link Short#getVideoInternalPath()} or {@link Short#getVideoUrl()},
   * and adds a cute loading animation until it is loaded.
   */
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    mVideoSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override public void surfaceCreated(SurfaceHolder holder) {
        Uri videoUri = mReactable.getVideoInternalPath() != null ? Uri.fromFile(
            new File(mReactable.getVideoInternalPath())) : Uri.parse(mReactable.getVideoUrl());
        mMediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(), videoUri,
            mVideoSurface.getHolder());
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
          @Override public void onPrepared(MediaPlayer mp) {
            getViewModel().onReady();
            mMediaPlayer.start();
          }
        });
        mMediaPlayer.setLooping(true);
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

      }

      @Override public void surfaceDestroyed(SurfaceHolder holder) {

      }
    });

    return mRootView;
  }

  @Override int getMediaFragmentResource() {
    return R.layout.fragment_short;
  }

  @OnTouch(R.id.videoSurface) boolean pauseOrResumeVideo(MotionEvent motionEvent) {
    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
      Log.v(TAG, "Short video paused.");
      mMediaPlayer.pause();
      return true;
    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
      Log.v(TAG, "Short video is resumed.. and action!");
      mMediaPlayer.start();
      return true;
    }
    return false;
  }
}
