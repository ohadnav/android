package com.truethat.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.view.fragment.VideoFragment;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Video.java</a>
 */

public class Video extends Media {
  public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
    @Override public Video createFromParcel(Parcel source) {
      return new Video(source);
    }

    @Override public Video[] newArray(int size) {
      return new Video[size];
    }
  };
  private static final String VIDEO_FILENAME = "video.mp4";
  private static final long serialVersionUID = 3860634597513193683L;
  /**
   * Internal path to video file on local storage.
   */
  private transient String mInternalPath;
  private transient @IdRes Integer mRawResourceId;

  public Video(@Nullable String internalPath) {
    mInternalPath = internalPath;
  }

  public Video(int rawResourceId) {
    mRawResourceId = rawResourceId;
  }

  private Video(Parcel in) {
    super(in);
    mInternalPath = (String) in.readValue(String.class.getClassLoader());
    mRawResourceId = (Integer) in.readValue(Integer.class.getClassLoader());
  }

  @VisibleForTesting public Video(@Nullable Long id, String url) {
    super(id, url);
  }

  public Integer getRawResourceId() {
    return mRawResourceId;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeValue(mInternalPath);
    dest.writeValue(mRawResourceId);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mInternalPath != null ? mInternalPath.hashCode() : 0);
    result = 31 * result + (mRawResourceId != null ? mRawResourceId.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Video)) return false;
    if (!super.equals(o)) return false;

    Video video = (Video) o;

    if (mInternalPath != null ? !mInternalPath.equals(video.mInternalPath)
        : video.mInternalPath != null) {
      return false;
    }
    return mRawResourceId != null ? mRawResourceId.equals(video.mRawResourceId)
        : video.mRawResourceId == null;
  }

  @Override public MediaFragment createFragment() {
    return VideoFragment.newInstance(this);
  }

  @Override MultipartBody.Part createPart() {
    if (mInternalPath == null) {
      throw new AssertionError("Video internal path had not been properly initialized.");
    }
    return MultipartBody.Part.createFormData(generatePartName(), VIDEO_FILENAME,
        RequestBody.create(MediaType.parse("video/mp4"), new File(mInternalPath)));
  }

  public String getInternalPath() {
    return mInternalPath;
  }
}
