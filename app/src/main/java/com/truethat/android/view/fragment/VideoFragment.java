package com.truethat.android.view.fragment;

import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.common.util.AppUtil;
import com.truethat.android.model.Video;
import java.io.File;
import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class VideoFragment extends MediaFragment<Video>
    implements TextureView.SurfaceTextureListener {
  @BindView(R.id.videoTextureView) TextureView mVideoTextureView;
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

  public MediaPlayer getMediaPlayer() {
    return mMediaPlayer;
  }

  /**
   * Displays the video from {@link Video#getInternalPath()} or {@link Video#getUrl()},
   * and adds a cute loading animation until it is loaded.
   */
  @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
      int height) {
    if (mMedia.getInternalPath() != null) {
      // Initialize media player from internal file, i.e. from the device's camera.
      mMediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(),
          Uri.fromFile(new File(mMedia.getInternalPath())));
      // Mirror video to give a more natural feel.
      mVideoTextureView.setScaleX(-1);
    } else {
      // Initialize media player and retriever from external URI, i.e. from other users videos.
      mMediaPlayer =
          MediaPlayer.create(getActivity().getApplicationContext(), Uri.parse(mMedia.getUrl()));
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(mMedia.getUrl(), new HashMap<String, String>());
    }
    if (mMediaPlayer == null) {
      throw new IllegalStateException("Failed to create media player for " + mMedia);
    }
    // Media player properties
    mMediaPlayer.setSurface(new Surface(surfaceTexture));
    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    // A callback that is invoked once the player is ready to start streaming.
    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        mLoadingImage.setVisibility(GONE);
        if (isVisibleAndResumed()
            && mVisibilityListener != null
            && mVisibilityListener.shouldBeVisible(VideoFragment.this)) {
          mp.start();
        }
        mIsReady = true;
        Log.d(TAG, "Video is prepared.");
        if (mMediaListener != null) {
          mMediaListener.onReady();
        }
      }
    });
    // Buffering callbacks to show loading indication while buffering.
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
    // Completion callbacks, that allows video looping and informing the listener.
    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mediaPlayer) {
        mHasFinished = true;
        if (mMediaListener != null) {
          mMediaListener.onFinished();
        }
        if (!mediaPlayer.isPlaying()) {
          mediaPlayer.start();
        }
        mediaPlayer.seekTo(0);
      }
    });
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    killMediaPlayer();
    return false;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  @Override public void onVisible() {
    super.onVisible();
    if (mIsReady) {
      mLoadingImage.setVisibility(GONE);
      if (mMediaPlayer != null) {
        mMediaPlayer.start();
      }
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    if (mMediaPlayer != null) {
      mMediaPlayer.pause();
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mVideoTextureView.setSurfaceTextureListener(this);
    // onSurfaceTextureAvailable does not get called if it is already available.
    if (mVideoTextureView.isAvailable()) {
      Size availableDisplaySize = AppUtil.availableDisplaySize(view);
      onSurfaceTextureAvailable(mVideoTextureView.getSurfaceTexture(),
          availableDisplaySize.getWidth(), availableDisplaySize.getHeight());
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    killMediaPlayer();
  }

  @Override int getLayoutResource() {
    return R.layout.fragment_video;
  }

  private void killMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }
}
