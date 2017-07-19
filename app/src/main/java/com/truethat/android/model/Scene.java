package com.truethat.android.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.view.fragment.ReactableFragment;
import com.truethat.android.view.fragment.SceneFragment;
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

public class Scene extends Reactable implements Serializable {
  private static final String IMAGE_FILENAME = "scene.jpg";
  /**
   * Signed URL to the scene's image on our storage.
   */
  private String mImageSignedUrl;
  /**
   * The byte representation of this scene. Used in {@link Reactable#createApiCall(StudioApi, Gson)}.
   */
  private transient byte[] mImageBytes;

  @VisibleForTesting public Scene(long id, String imageSignedUrl, User director,
      TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction) {
    super(id, director, reactionCounters, created, userReaction);
    mImageSignedUrl = imageSignedUrl;
  }

  public Scene(User director, byte[] imageBytes) {
    super(director, new Date());
    mImageBytes = imageBytes;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") private Scene() {
  }

  public String getImageSignedUrl() {
    return mImageSignedUrl;
  }

  @Override public ReactableFragment createFragment() {
    return SceneFragment.newInstance(this);
  }

  @Override public Call<Reactable> createApiCall(StudioApi studioApi, Gson gson) {
    if (mImageBytes == null) {
      throw new AssertionError("Image bytes had not been properly initialized.");
    }
    MultipartBody.Part imagePart =
        MultipartBody.Part.createFormData(StudioApi.SCENE_IMAGE_PART, IMAGE_FILENAME,
            RequestBody.create(MediaType.parse("image/jpg"), mImageBytes));
    MultipartBody.Part reactablePart =
        MultipartBody.Part.createFormData(StudioApi.REACTABLE_PART, gson.toJson(this));
    return studioApi.saveReactable(reactablePart, Collections.singletonList(imagePart));
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;
    if (!super.equals(o)) return false;

    Scene scene = (Scene) o;

    return mImageSignedUrl != null ? mImageSignedUrl.equals(scene.mImageSignedUrl)
        : scene.mImageSignedUrl == null;
  }
}
