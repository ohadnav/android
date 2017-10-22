package com.truethat.android.view.fragment;

import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.view.activity.MainActivity;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Test;

import static com.truethat.android.common.network.NetworkUtil.GSON;
import static com.truethat.android.view.fragment.ScenesPagerFragmentTest.assertSceneDisplayed;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterFragmentTest extends BaseInstrumentationTestSuite {
  private List<Scene> mRespondedScenes;
  private Scene mScene;

  @Override public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    mScene =
        new Scene(1L, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(), new Date(),
            new Photo(2L, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg"));
    mRespondedScenes = Collections.singletonList(mScene);
    MainActivity.sLaunchIndex = MainActivity.TOOLBAR_THEATER_INDEX;
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
  }

  @Override public void tearDown() throws Exception {
    super.tearDown();
    MainActivity.sLaunchIndex = MainActivity.TOOLBAR_STUDIO_INDEX;
  }

  @Test public void displayScene() throws Exception {
    assertSceneDisplayed(mScene, 2L);
  }
}