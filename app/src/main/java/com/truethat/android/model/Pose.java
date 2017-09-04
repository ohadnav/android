package com.truethat.android.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.view.fragment.PoseFragment;
import com.truethat.android.view.fragment.ReactableFragment;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public class Pose extends Reactable implements Serializable {
  private static final String IMAGE_FILENAME = "pose.jpg";
  private static final long serialVersionUID = 1L;
  /**
   * Signed URL to the pose's image on our storage.
   */
  private String mImageUrl;
  /**
   * The byte representation of this pose. Used in {@link Reactable#createApiCall()}.
   */
  private transient byte[] mImageBytes;

  @VisibleForTesting
  public Pose(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction, String imageUrl) {
    super(id, director, reactionCounters, created, userReaction);
    mImageUrl = imageUrl;
  }

  public Pose(User director, byte[] imageBytes) {
    super(director, new Date());
    mImageBytes = imageBytes;
  }

  @VisibleForTesting public Pose(byte[] imageBytes) {
    mImageBytes = imageBytes;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") private Pose() {
  }

  public String getImageUrl() {
    return mImageUrl;
  }

  public byte[] getImageBytes() {
    return mImageBytes;
  }

  @Override public ReactableFragment createFragment() {
    return PoseFragment.newInstance(this);
  }

  @Override public Call<Reactable> createApiCall() {
    if (mImageBytes == null) {
      throw new AssertionError("Image bytes had not been properly initialized.");
    }
    MultipartBody.Part imagePart =
        MultipartBody.Part.createFormData(StudioApi.POSE_IMAGE_PART, IMAGE_FILENAME,
            RequestBody.create(MediaType.parse("image/jpg"), mImageBytes));
    MultipartBody.Part reactablePart =
        MultipartBody.Part.createFormData(StudioApi.REACTABLE_PART, NetworkUtil.GSON.toJson(this));
    return NetworkUtil.createApi(StudioApi.class)
        .saveReactable(reactablePart, Collections.singletonList(imagePart));
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pose)) return false;
    if (!super.equals(o)) return false;

    Pose pose = (Pose) o;

    return mImageUrl != null ? mImageUrl.equals(pose.mImageUrl) : pose.mImageUrl == null;
  }
}
