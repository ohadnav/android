package com.truethat.android.view.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import static android.view.View.GONE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 *
 * A UI container representation of {@link Photo}.
 */

public class PhotoFragment extends MediaFragment<Photo> {
  @BindView(R.id.imageView) ImageView mImageView;

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
      mImageView.setImageBitmap(scaledBitmap);
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
              mLoadingImage.setVisibility(GONE);
            }

            @Override public void onError() {
              Log.w(TAG, "Failed to download image.");
            }
          });
    }
  }

  @Override int getLayoutResource() {
    return R.layout.fragment_photo;
  }
}
