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
import com.truethat.android.model.Video;
import java.io.File;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class VideoFragment extends MediaFragment<Video> {
  @BindView(R.id.videoSurface) SurfaceView mVideoSurface;
  private MediaPlayer mMediaPlayer;

  public VideoFragment() {
    // Required empty public constructor
  }

  public static VideoFragment newInstance(Video video) {
    VideoFragment videoFragment = new VideoFragment();
    MediaFragment.prepareInstance(videoFragment, video);
    videoFragment.mAutomaticViewBinding = false;
    return videoFragment;
  }

  @Override public void onVisible() {
    super.onVisible();
    if (mIsReady) {
      mLoadingImage.setVisibility(GONE);
      mMediaPlayer.start();
    }
  }

  public MediaPlayer getMediaPlayer() {
    return mMediaPlayer;
  }

  /**
   * Displays the video from {@link Video#getInternalPath()} or {@link Video#getUrl()},
   * and adds a cute loading animation until it is loaded.
   */
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    mVideoSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override public void surfaceCreated(SurfaceHolder holder) {
        Uri videoUri =
            mMedia.getInternalPath() != null ? Uri.fromFile(new File(mMedia.getInternalPath()))
                : Uri.parse(mMedia.getUrl());
        mMediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(), videoUri,
            mVideoSurface.getHolder());
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
          @Override public void onPrepared(MediaPlayer mp) {
            mLoadingImage.setVisibility(GONE);
            mp.start();
            mIsReady = true;
            Log.d(TAG, "Video is prepared.");
            if (mMediaListener != null) {
              mMediaListener.onReady();
            }
          }
        });
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
          @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
              case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "Video is buffering.");
                mLoadingImage.setVisibility(VISIBLE);
                break;
              case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "Video buffering completed.");
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

  @Override int getLayoutResource() {
    return R.layout.fragment_video;
  }

  @OnTouch(R.id.videoSurface) boolean pauseOrResumeVideo(MotionEvent motionEvent) {
    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
      Log.d(TAG, "Video video paused.");
      mMediaPlayer.pause();
      return true;
    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
      Log.d(TAG, "Video video is resumed.. and action!");
      mMediaPlayer.start();
      return true;
    }
    return false;
  }
}
