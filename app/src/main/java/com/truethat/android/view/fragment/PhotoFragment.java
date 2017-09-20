package com.truethat.android.view.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.truethat.android.R;
import com.truethat.android.common.util.AppUtil;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Photo;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 * <p>
 * A UI container representation of {@link Photo}.
 */

public class PhotoFragment extends MediaFragment<Photo> {
  private static final long FINISHED_TIMEOUT_MILLIS = 1000;
  @BindView(R.id.imageView) ImageView mImageView;
  private Timer mTimer;

  public PhotoFragment() {
    // Required empty public constructor
  }

  public static PhotoFragment newInstance(Photo photo) {
    PhotoFragment photoFragment = new PhotoFragment();
    MediaFragment.prepareInstance(photoFragment, photo);
    photoFragment.mAutomaticViewBinding = false;
    return photoFragment;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (mMedia.getBytes() != null) {
      Bitmap originalBitmap =
          BitmapFactory.decodeByteArray(mMedia.getBytes(), 0, mMedia.getBytes().length);
      Point scaledSize =
          CameraUtil.scaleFit(new Point(originalBitmap.getWidth(), originalBitmap.getHeight()),
              AppUtil.realDisplaySize(getActivity()));
      Bitmap scaledBitmap =
          Bitmap.createScaledBitmap(originalBitmap, scaledSize.x, scaledSize.y, false);
      // Flip images that were taken with the camera
      Matrix flipMatrix = new Matrix();
      flipMatrix.preScale(-1, 1);
      Bitmap flipped =
          Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
              flipMatrix, false);
      mImageView.setImageBitmap(flipped);
      mLoadingImage.setVisibility(GONE);
      if (mMediaListener != null) {
        mMediaListener.onReady();
      }
      mIsReady = true;
    } else {
      Picasso.with(getContext())
          .load(mMedia.getUrl())
          .centerCrop()
          .resize(AppUtil.realDisplaySize(getContext()).x, AppUtil.realDisplaySize(getContext()).y)
          .into(mImageView, new Callback() {
            @Override public void onSuccess() {
              Log.d(TAG, "Image downloaded.");
              if (mMediaListener != null) {
                mMediaListener.onReady();
              }
              mIsReady = true;
              if (mLoadingImage != null) {
                mLoadingImage.setVisibility(GONE);
              }
            }

            @Override public void onError() {
              Log.w(TAG, "Failed to download image.");
            }
          });
    }
  }

  @Override public void onVisible() {
    super.onVisible();
    if (mTimer == null) mTimer = new Timer(TAG);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        mHasFinished = true;
        if (mMediaListener != null) {
          mMediaListener.onFinished();
        }
      }
    }, FINISHED_TIMEOUT_MILLIS);
  }

  @Override public void onHidden() {
    super.onHidden();
    if (mTimer != null) {
      mTimer.cancel();
      mTimer.purge();
      mTimer = null;
    }
  }

  @Override int getLayoutResource() {
    return R.layout.fragment_photo;
  }
}
