package com.truethat.android.view.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.truethat.android.R;
import com.truethat.android.common.util.AppUtil;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Pose;
import com.truethat.android.viewmodel.ReactableViewModel;

import static android.view.View.GONE;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class PoseFragment
    extends ReactableFragment<Pose, ReactableViewModel<Pose>, FragmentReactableBinding> {
  @BindView(R.id.poseImage) ImageView mImageView;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;

  public PoseFragment() {
    // Required empty public constructor
  }

  public static PoseFragment newInstance(Pose pose) {
    PoseFragment poseFragment = new PoseFragment();
    ReactableFragment.prepareInstance(poseFragment, pose);
    poseFragment.mAutomaticViewBinding = false;
    return poseFragment;
  }

  /**
   * Displays the image from {@link Pose#getImageUrl()}, and adds a cute loading animation
   * until it is loaded.
   */
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    if (mReactable.getImageBytes() != null) {
      Bitmap originalBitmap = BitmapFactory.decodeByteArray(mReactable.getImageBytes(), 0,
          mReactable.getImageBytes().length);
      Point scaledSize =
          CameraUtil.scaleFit(new Point(originalBitmap.getWidth(), originalBitmap.getHeight()),
              AppUtil.realDisplaySize(getActivity()));
      Bitmap scaledBitmap =
          Bitmap.createScaledBitmap(originalBitmap, scaledSize.x, scaledSize.y, false);
      mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
      mImageView.setImageBitmap(scaledBitmap);
      mLoadingImage.setVisibility(GONE);
      getViewModel().onReady();
    } else {
      Glide.with(getContext())
          .load(mReactable.getImageUrl())
          .centerCrop()
          .listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                boolean isFirstResource) {
              return false;
            }

            @Override public boolean onResourceReady(GlideDrawable resource, String model,
                Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
              getViewModel().onReady();
              mLoadingImage.setVisibility(GONE);
              return false;
            }
          })
          .into(mImageView);
    }
    return mRootView;
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override int getMediaFragmentResource() {
    return R.layout.fragment_pose;
  }
}
