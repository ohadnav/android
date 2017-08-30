package com.truethat.android.view.custom;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public class DataBindingAdapters {
  // --------------------------- ImageView -----------------------------
  @SuppressWarnings("unused") @BindingAdapter("android:src") public static void setImageUrl(
      ImageView view, String imageUrl) {
    Glide.with(view.getContext()).load(imageUrl).into(view);
  }

  @BindingAdapter("android:src")
  public static void setImageResource(ImageView imageView, int resource) {
    imageView.setImageResource(resource);
  }

  // --------------------------- EditText -----------------------------

  /**
   * @param inputType taken from {@link InputType}.
   */
  @BindingAdapter("android:inputType") public static void setInputType(EditText view,
      int inputType) {
    view.setInputType(inputType);
  }

  // --------------------------- ViewPager ------------------------------
  @SuppressWarnings("unchecked") @BindingAdapter("items") public static void attachItems(
      ViewPager viewPager, ObservableList items) {
    if (viewPager.getAdapter() != null && viewPager.getAdapter() instanceof BaseFragmentAdapter) {
      ((BaseFragmentAdapter) viewPager.getAdapter()).setItems(items);
    }
  }

  // --------------------------- Colors ----------------------------------
  @BindingAdapter("android:textColor") public static void setTextColor(TextView textView,
      @ColorRes int colorResId) {
    textView.setTextColor(ResourcesCompat.getColor(textView.getResources(), colorResId,
        textView.getContext().getTheme()));
  }

  @BindingAdapter("android:backgroundTint")
  public static void setBackgroundTint(View view, @ColorRes int colorResId) {
    view.setBackgroundTintList(ColorStateList.valueOf(
        view.getResources().getColor(colorResId, view.getContext().getTheme())));
  }
}