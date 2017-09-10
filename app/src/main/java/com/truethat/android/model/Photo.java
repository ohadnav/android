package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.view.fragment.PhotoFragment;
import java.io.Serializable;
import java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public class Photo extends Media implements Serializable {
  private static final String IMAGE_FILENAME = "photo.jpg";
  private static final long serialVersionUID = 7108700087565678875L;
  private transient byte[] mBytes;

  public Photo(@Nullable String url, @Nullable byte[] bytes) {
    super(url);
    mBytes = bytes;
  }

  public byte[] getBytes() {
    return mBytes;
  }

  @Override public MediaFragment createFragment() {
    return PhotoFragment.newInstance(this);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(mBytes);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Photo)) return false;
    if (!super.equals(o)) return false;

    Photo photo = (Photo) o;

    return Arrays.equals(mBytes, photo.mBytes);
  }

  @Override MultipartBody.Part createPart() {
    if (mBytes == null) {
      throw new AssertionError("Image bytes had not been properly initialized.");
    }
    return MultipartBody.Part.createFormData(StudioApi.MEDIA_PART, IMAGE_FILENAME,
        RequestBody.create(MediaType.parse("image/jpg"), mBytes));
  }
}
