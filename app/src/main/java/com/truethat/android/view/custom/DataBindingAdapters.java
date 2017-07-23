package com.truethat.android.view.custom;

import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public class DataBindingAdapters {
  @BindingAdapter("android:src") public static void setImageUrl(ImageView view, String imageUrl) {
    Glide.with(view.getContext()).load(imageUrl).into(view);
  }

  @BindingAdapter("android:src") public static void setImageUri(ImageView view, Uri imageUri) {
    view.setImageURI(imageUri);
  }

  @BindingAdapter("android:src")
  public static void setImageDrawable(ImageView view, Drawable drawable) {
    view.setImageDrawable(drawable);
  }

  @BindingAdapter("android:src")
  public static void setImageResource(ImageView imageView, int resource) {
    imageView.setImageResource(resource);
  }

  @SuppressWarnings("unchecked") @BindingAdapter("items")
  public static void attachItems(ViewPager viewPager, ObservableList items) {
    if (viewPager.getAdapter() != null && viewPager.getAdapter() instanceof BaseFragmentAdapter) {
      ((BaseFragmentAdapter) viewPager.getAdapter()).setItems(items);
    }
  }
}