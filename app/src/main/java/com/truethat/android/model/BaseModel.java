package com.truethat.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.truethat.android.common.network.NetworkUtil;
import java.io.Serializable;

/**
 * Proudly created by ohad on 14/09/2017 for TrueThat.
 */

class BaseModel implements Serializable, Parcelable {
  public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {
    @Override public BaseModel createFromParcel(Parcel source) {
      return new BaseModel(source);
    }

    @Override public BaseModel[] newArray(int size) {
      return new BaseModel[size];
    }
  };
  private static final long serialVersionUID = -7997726900146734179L;
  /**
   * As stored in our backend. ID is optional.
   */
  Long mId;

  @SuppressWarnings({ "unused" }) BaseModel() {
    // Empty constructor for serialization and deserialization.
  }

  BaseModel(@Nullable Long id) {
    mId = id;
  }

  BaseModel(Parcel in) {
    mId = (Long) in.readValue(Long.class.getClassLoader());
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(mId);
  }

  public Long getId() {
    return mId;
  }

  public void setId(Long id) {
    mId = id;
  }

  @Override public int hashCode() {
    return mId != null ? mId.hashCode() : 0;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BaseModel)) return false;

    BaseModel baseModel = (BaseModel) o;

    return mId != null ? mId.equals(baseModel.mId) : baseModel.mId == null;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }
}
