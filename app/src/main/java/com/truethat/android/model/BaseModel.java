package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.NetworkUtil;
import java.io.Serializable;

/**
 * Proudly created by ohad on 14/09/2017 for TrueThat.
 */

abstract class BaseModel implements Serializable {
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
