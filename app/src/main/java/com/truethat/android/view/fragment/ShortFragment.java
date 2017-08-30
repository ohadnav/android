package com.truethat.android.view.fragment;

import android.graphics.drawable.AnimationDrawable;
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
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnTouch;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Short;
import com.truethat.android.viewmodel.ReactableViewModel;
import java.io.File;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class ShortFragment
    extends ReactableFragment<Short, ReactableViewModel<Short>, FragmentReactableBinding> {
  @BindView(R.id.videoSurface) SurfaceView mVideoSurface;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private MediaPlayer mMediaPlayer;

  public ShortFragment() {
    // Required empty public constructor
  }

  public static ShortFragment newInstance(Short aShort) {
    ShortFragment shortFragment = new ShortFragment();
    ReactableFragment.prepareInstance(shortFragment, aShort);
    shortFragment.mAutomaticViewBinding = false;
    return shortFragment;
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onVisible() {
    super.onVisible();
    if (getViewModel().isReady()) {
      mLoadingImage.setVisibility(GONE);
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
            mLoadingImage.setVisibility(GONE);
            mMediaPlayer.start();
            getViewModel().onReady();
          }
        });
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
          @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
              case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                mLoadingImage.setVisibility(VISIBLE);
                break;
              case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mLoadingImage.setVisibility(GONE);
                break;
            }
            return false;
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
