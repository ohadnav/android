package com.truethat.android.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.view.fragment.VideoFragment;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Proudly created by ohad on 24/08/2017 for TrueThat.
 */

public class Short extends Reactable {
  private static final String VIDEO_FILENAME = "short.mp4";
  private static final long serialVersionUID = -7669202029690134610L;
  /**
   * Signed URL to the pose's image on our storage.
   */
  @SuppressWarnings("unused") private String mVideoUrl;
  /**
   * Internal path to video file.
   */
  private transient String mVideoInternalPath;

  @VisibleForTesting
  public Short(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction, String videoUrl) {
    super(id, director, reactionCounters, created, userReaction);
    mVideoUrl = videoUrl;
  }

  public Short(User director, String videoInternalPath) {
    super(director, new Date());
    mVideoInternalPath = videoInternalPath;
  }

  @Override public MediaFragment createMediaFragment() {
    return VideoFragment.newInstance(new Video(mVideoUrl, mVideoInternalPath));
  }

  @Override public Call<Reactable> createApiCall() {
    if (mVideoInternalPath == null) {
      throw new AssertionError("Video internal path had not been properly initialized.");
    }
    MultipartBody.Part imagePart =
        MultipartBody.Part.createFormData(StudioApi.SHORT_VIDEO_PART, VIDEO_FILENAME,
            RequestBody.create(MediaType.parse("video/mp4"), new File(mVideoInternalPath)));
    MultipartBody.Part reactablePart =
        MultipartBody.Part.createFormData(StudioApi.REACTABLE_PART, NetworkUtil.GSON.toJson(this));
    return NetworkUtil.createApi(StudioApi.class)
        .saveReactable(reactablePart, Collections.singletonList(imagePart));
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Short)) return false;
    if (!super.equals(o)) return false;

    Short aShort = (Short) o;

    return mVideoUrl != null ? mVideoUrl.equals(aShort.mVideoUrl) : aShort.mVideoUrl == null;
  }

  public String getVideoUrl() {
    return mVideoUrl;
  }

  public String getVideoInternalPath() {
    return mVideoInternalPath;
  }

  @Override public int hashCode() {
    return mVideoUrl != null ? mVideoUrl.hashCode() : 0;
  }
}
