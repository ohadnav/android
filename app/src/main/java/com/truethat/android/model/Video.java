package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.view.fragment.VideoFragment;
import java.io.File;
import java.io.Serializable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public class Video extends Media implements Serializable {
  private static final String VIDEO_FILENAME = "video.mp4";
  private static final long serialVersionUID = 3860634597513193683L;
  /**
   * Internal path to video file on local storage.
   */
  private transient String mInternalPath;

  public Video(@Nullable String url, @Nullable String internalPath) {
    super(url);
    mInternalPath = internalPath;
  }

  public String getInternalPath() {
    return mInternalPath;
  }

  @Override public MediaFragment createFragment() {
    return VideoFragment.newInstance(this);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mInternalPath != null ? mInternalPath.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Video)) return false;
    if (!super.equals(o)) return false;

    Video video = (Video) o;

    return mInternalPath != null ? mInternalPath.equals(video.mInternalPath)
        : video.mInternalPath == null;
  }

  @Override MultipartBody.Part createPart(String partName) {
    if (mInternalPath == null) {
      throw new AssertionError("Video internal path had not been properly initialized.");
    }
    return MultipartBody.Part.createFormData(partName, VIDEO_FILENAME,
        RequestBody.create(MediaType.parse("video/mp4"), new File(mInternalPath)));
  }
}
