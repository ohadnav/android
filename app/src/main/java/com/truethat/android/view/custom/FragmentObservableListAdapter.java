package com.truethat.android.view.custom;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public abstract class FragmentObservableListAdapter<T> extends BaseFragmentAdapter {
  ObservableList<T> mItems;

  FragmentObservableListAdapter(@NonNull FragmentManager fm, FragmentViewPager fragmentViewPager) {
    super(fm, fragmentViewPager);
    setItems(new ObservableArrayList<T>());
  }

  public ObservableList<T> getItems() {
    return mItems;
  }

  public void setItems(ObservableList<T> items) {
    mItems = items;
    mItems.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<T>>() {
      @Override public void onChanged(ObservableList sender) {
        FragmentObservableListAdapter.this.notifyDataSetChanged();
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

  @Override public int getCount() {
    return mItems.size();
  }
}
