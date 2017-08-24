package com.truethat.android.view.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Pose;
import com.truethat.android.viewmodel.ReactableViewModel;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class PoseFragment
    extends ReactableFragment<Pose, ReactableViewModel<Pose>, FragmentReactableBinding> {
  public PoseFragment() {
    // Required empty public constructor
  }

  public static PoseFragment newInstance(Pose pose) {
    PoseFragment poseFragment = new PoseFragment();
    ReactableFragment.prepareInstance(poseFragment, pose);
    return poseFragment;
  }

  /**
   * Displays the image from {@link Pose#getImageSignedUrl()}, and adds a cute loading animation
   * until it is loaded.
   */
  @Override protected void createMedia(LayoutInflater inflater) {
    View poseLayout = inflater.inflate(R.layout.fragment_pose,
        (ViewGroup) mRootView.findViewById(R.id.mediaLayout));
    final ImageView imageView = (ImageView) poseLayout.findViewById(R.id.poseImage);
    final AnimationDrawable animationDrawable =
        (AnimationDrawable) getContext().getDrawable(R.drawable.anim_loading_elephant);
    if (animationDrawable == null) {
      throw new AssertionError("Loading resource not found.. Where are my elephants?!");
    }
    animationDrawable.start();
    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    Glide.with(getContext()).load(mReactable.getImageSignedUrl())
        .placeholder(animationDrawable)
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
            return false;
          }
        })
        .into(imageView);
  }
}
