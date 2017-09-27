package com.truethat.android.view.fragment;

import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.model.Video;
import java.io.File;
import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class VideoFragment extends MediaFragment<Video> {
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
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    mVideoTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
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
          // Sets proper orientation
          int videoOrientation = Integer.parseInt(
              retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
          // iPhone videos has the "correct" orientation of 0, and so need to be rotated and scaled
          // to match the android videos.
          if (videoOrientation == 0) {
            mVideoTextureView.setRotation(90F);
            // 0.01 is added to compensate for missing pixel row
            mVideoTextureView.setScaleX(0.01F
                + Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                / Float.parseFloat(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            mVideoTextureView.setScaleY(Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)) / Float
                .parseFloat(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
          }
        }
        if (mMediaPlayer == null) {
          throw new IllegalStateException("Failed to create media player for " + mMedia);
        }
        // Media player properties
        mMediaPlayer.setSurface(new Surface(surface));
        mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        // A callback that is invoked once the player is ready to start streaming.
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
          @Override public void onPrepared(MediaPlayer mp) {
            mLoadingImage.setVisibility(GONE);
            if (isVisible() && (mMediaListener == null || mMediaListener.isReallyVisible())) {
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

      @Override
      public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

      }

      @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        killMediaPlayer();
        return false;
      }

      @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

      }
    });

    return mRootView;
  }

  @Override int getLayoutResource() {
    return R.layout.fragment_video;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    killMediaPlayer();
  }

  @Override public void onVisible() {
    super.onVisible();
    if (mIsReady) {
      mLoadingImage.setVisibility(GONE);
      if (mMediaPlayer != null && (mMediaListener == null || mMediaListener.isReallyVisible())) {
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

  private void killMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }
}
