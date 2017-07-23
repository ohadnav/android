package com.truethat.android.view.custom;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import eu.inloop.viewmodel.support.ViewModelStatePagerAdapter;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

abstract class BaseFragmentAdapter<T> extends ViewModelStatePagerAdapter {
  ObservableList<T> mItems;

  BaseFragmentAdapter(@NonNull FragmentManager fm) {
    super(fm);
    setItems(new ObservableArrayList<T>());
  }

  @Override public int getCount() {
    return mItems.size();
  }

  void setItems(ObservableList<T> items) {
    mItems = items;
    mItems.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<T>>() {
      @Override public void onChanged(ObservableList sender) {
        BaseFragmentAdapter.this.notifyDataSetChanged();
      }

      @Override
      public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        onChanged(sender);
      }

      @Override
      public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
        onChanged(sender);
      }

      @Override
      public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition,
          int itemCount) {
        onChanged(sender);
      }

      @Override
      public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
        onChanged(sender);
      }
    });
    notifyDataSetChanged();
  }
}
