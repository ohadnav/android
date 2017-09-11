package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.view.fragment.MediaFragment;
import java.io.Serializable;
import okhttp3.MultipartBody;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 *
 * Media items such as videos or photos.
 * <p>
 * Each implementation should register at {@link NetworkUtil#GSON}.
 */

public abstract class Media implements Serializable {
  private static final long serialVersionUID = 6966859003865108004L;
  private String mUrl;

  Media(@Nullable String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  /**
   * @return a view displaying this media.
   */
  public abstract MediaFragment createFragment();

  @Override public int hashCode() {
    return mUrl != null ? mUrl.hashCode() : 0;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Media)) return false;

    Media media = (Media) o;

    return mUrl != null ? mUrl.equals(media.mUrl) : media.mUrl == null;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }

  /**
   * @return a HTTP multipart part with the binary data of this media.
   */
  abstract MultipartBody.Part createPart(String partName);
}
