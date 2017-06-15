package com.truethat.android.common;

import android.media.Image;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.auth.MockAuthModule;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.ReactionDetectionModule;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import org.junit.BeforeClass;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 *
 * BaseApplicationTest suite. Initializes mock application modules, and more.
 */

@SuppressWarnings({ "FieldCanBeLocal", "WeakerAccess" }) public class BaseApplicationTest {
  protected static final Emotion DEFAULT_DETECTED_EMOTION = Emotion.HAPPY;
  protected static MockPermissionsModule sMockPermissionsModule;
  protected static MockAuthModule sMockAuthModule;
  protected static MockInternalStorage sMockInternalStorage;
  protected static ReactionDetectionModule sReactionDetectionModule;

  @BeforeClass public static void beforeClass() throws Exception {
    // Sets up the mocked permissions module.
    App.setPermissionsModule(sMockPermissionsModule = new MockPermissionsModule());
    // Sets up the mocked auth module.
    App.setAuthModule(sMockAuthModule = new MockAuthModule());
    // Sets up the mocked in-mem internal storage.
    App.setInternalStorage(sMockInternalStorage = new MockInternalStorage());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(sReactionDetectionModule = new ReactionDetectionModule() {
      @Override public void detect(ReactionDetectionPubSub detectionPubSub) {
        detectionPubSub.onReactionDetected(DEFAULT_DETECTED_EMOTION);
      }

      @Override public void attempt(Image image) {

      }

      @Override public void stop() {

      }
    });
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost");
  }
}
