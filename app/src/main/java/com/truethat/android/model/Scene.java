package com.truethat.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.ui.common.media.ReactableFragment;
import com.truethat.android.ui.common.media.SceneFragment;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public class Scene extends Reactable implements Serializable {

  /**
   * Internal storage directory that contains serialized scenes.
   */
  private static final String CREATED_SCENES_PATH = "studio/scenes/";
  /**
   * Internal storage suffix for scenes.
   */
  private static final String SCENE_SUFFIX = ".scene";

  /**
   * Signed URL to the scene's image on our storage.
   */
  private String mImageSignedUrl;

  @VisibleForTesting public Scene(long id, String imageSignedUrl, User director,
      @NonNull TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction) {
    super(id, director, reactionCounters, created, userReaction);
    mImageSignedUrl = imageSignedUrl;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") private Scene() {
  }

  public static String internalStoragePath(long id) {
    return CREATED_SCENES_PATH + id + SCENE_SUFFIX;
  }

  public String getImageSignedUrl() {
    return mImageSignedUrl;
  }

  /**
   * @return The relative path of the scene within the app's internal storage.
   */
  public String internalStoragePath() {
    return internalStoragePath(getId());
  }

  @Override public ReactableFragment createFragment() {
    return SceneFragment.newInstance(this);
  }
}
