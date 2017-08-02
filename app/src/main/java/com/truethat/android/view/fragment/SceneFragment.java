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
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.ReactableViewModel;

/**
 * Proudly created by ohad on 21/06/2017 for TrueThat.
 */

public class SceneFragment
    extends ReactableFragment<Scene, ReactableViewModel<Scene>, FragmentReactableBinding> {
  public SceneFragment() {
    // Required empty public constructor
  }

  public static SceneFragment newInstance(Scene scene) {
    SceneFragment sceneFragment = new SceneFragment();
    ReactableFragment.prepareInstance(sceneFragment, scene);
    return sceneFragment;
  }

  /**
   * Displays the image from {@link Scene#getImageSignedUrl()}, and adds a cute loading animation
   * until it is loaded.
   */
  @Override protected void createMedia(LayoutInflater inflater) {
    View sceneLayout = inflater.inflate(R.layout.fragment_scene,
        (ViewGroup) mRootView.findViewById(R.id.mediaLayout));
    final ImageView imageView = (ImageView) sceneLayout.findViewById(R.id.sceneImage);
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