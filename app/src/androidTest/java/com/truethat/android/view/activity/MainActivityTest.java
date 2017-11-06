//package com.truethat.android.view.activity;
//
//import android.support.test.espresso.action.ViewActions;
//import com.truethat.android.application.AppContainer;
//import com.truethat.android.common.BaseInstrumentationTestSuite;
//import com.truethat.android.common.network.NetworkUtil;
//import com.truethat.android.common.network.RepertoireApi;
//import com.truethat.android.common.network.TheaterApi;
//import com.truethat.android.empathy.AffectivaReactionDetectionManager;
//import com.truethat.android.model.Photo;
//import com.truethat.android.model.Scene;
//import com.truethat.android.model.User;
//import com.truethat.android.model.Video;
//import com.truethat.android.view.fragment.CameraFragment;
//import java.util.Collections;
//import java.util.Date;
//import okhttp3.mockwebserver.Dispatcher;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.RecordedRequest;
//import org.awaitility.core.ThrowingRunnable;
//import org.junit.Test;
//
//import static android.support.test.InstrumentationRegistry.getInstrumentation;
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static com.truethat.android.view.fragment.ScenesPagerFragmentTest.assertSceneDisplayed;
//import static com.truethat.android.view.fragment.StudioActivityTest.assertEditState;
//import static org.awaitility.Awaitility.await;
//import static org.junit.Assert.assertEquals;
//
///**
// * Proudly created by ohad on 10/10/2017 for TrueThat.
// */
//public class MainActivityTest extends BaseInstrumentationTestSuite {
//  private Scene mPhotoScene;
//  private Scene mVideoScene;
//
//  @Override public void setUp() throws Exception {
//    super.setUp();
//    mPhotoScene = new Scene(1L, mFakeAuthManager.getCurrentUser(), null, new Date(),
//        new Photo(1L, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg"));
//    mVideoScene = new Scene(2L, new User(2L, "moshik", "afya", "mashu-mashu"), null, new Date(),
//        new Video(2L,
//            "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4"));
//    // Ensures backend return a photo scene in repertoire and a video one in the theater.
//    mMockWebServer.setDispatcher(new Dispatcher() {
//      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
//        MockResponse mockResponse = new MockResponse();
//        if (request.getPath().endsWith(RepertoireApi.PATH)) {
//          mockResponse.setBody(NetworkUtil.GSON.toJson(Collections.singletonList(mPhotoScene)));
//        } else if (request.getPath().endsWith(TheaterApi.PATH)) {
//          mockResponse.setBody(NetworkUtil.GSON.toJson(Collections.singletonList(mVideoScene)));
//        }
//        return mockResponse;
//      }
//    });
//    mMainActivityRule.launchActivity(null);
//  }
//
//  @Test public void navigateWithSwipes() throws Exception {
//    assertStudio();
//    // Swipe right to repertoire
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeRight());
//    assertRepertoire();
//    // Swipe left back to studio
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeLeft());
//    assertStudio();
//    // Swipe left to theater
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeLeft());
//    assertTheater();
//  }
//
//  @Test public void navigateWithToolbar() throws Exception {
//    assertStudio();
//    // Click on repertoire icon
//    getInstrumentation().runOnMainSync(new Runnable() {
//      @Override public void run() {
//        mMainActivityRule.getActivity().mToolbarRepertoire.performClick();
//      }
//    });
//    assertRepertoire();
//    // Click on theater icon
//    getInstrumentation().runOnMainSync(new Runnable() {
//      @Override public void run() {
//        mMainActivityRule.getActivity().mToolbarTheater.performClick();
//      }
//    });
//    assertTheater();
//    // Go back to studio with toolbar icon
//    getInstrumentation().runOnMainSync(new Runnable() {
//      @Override public void run() {
//        mMainActivityRule.getActivity().mToolbarStudio.performClick();
//      }
//    });
//    assertStudio();
//  }
//
//  @Test public void navigateOnStudioEdit() throws Exception {
//    assertStudio();
//    // Take a picture
//    getInstrumentation().runOnMainSync(new Runnable() {
//      @Override public void run() {
//        mMainActivityRule.getActivity().mToolbarStudio.performClick();
//      }
//    });
//    // Should proceed to edit state
//    assertEditState();
//    // Swipe right to repertoire
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeRight());
//    assertRepertoire();
//  }
//
//  @Test public void navigateWhenUsingRealCameraEmotionDetection() throws Exception {
//    AppContainer.setReactionDetectionManager(
//        new AffectivaReactionDetectionManager(mMainActivityRule.getActivity(),
//            AppContainer.getPermissionsManager()));
//    assertStudio();
//    // Swipe right to repertoire
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeRight());
//    assertRepertoire();
//    // Go back to studio
//    onView(withId(android.R.id.content)).perform(ViewActions.swipeLeft());
//    assertStudio();
//  }
//
//  private void assertStudio() {
//    waitForMainFragment(MainActivity.TOOLBAR_STUDIO_INDEX);
//    // Wait until camera preview is live.
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(CameraFragment.CameraState.PREVIEW,
//            ((CameraFragment) mMainActivityRule.getActivity()
//                .getStudioFragment()
//                .getFragmentManager()
//                .findFragmentByTag(CameraFragment.FRAGMENT_TAG)).getState());
//      }
//    });
//    // Assert scale of capture button
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_STUDIO_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_STUDIO_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleY(), 0.01);
//      }
//    });
//    // Assert scale of lateral toolbar buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleY(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleY(), 0.01);
//      }
//    });
//    // Assert translation of lateral toolbar buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(0f, mMainActivityRule.getActivity().getToolbarTheater().getTranslationX(),
//            0.01);
//        assertEquals(0f, mMainActivityRule.getActivity().getToolbarRepertoire().getTranslationX(),
//            0.01);
//      }
//    });
//  }
//
//  private void assertRepertoire() {
//    waitForMainFragment(MainActivity.TOOLBAR_REPERTOIRE_INDEX);
//    // Assert scale of studio and theater buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_STUDIO_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_STUDIO_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleY(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleY(), 0.01);
//      }
//    });
//    // Assert scale of repertoire toolbar button
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_LATERAL_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleY(), 0.01);
//      }
//    });
//    // Assert translation of lateral toolbar buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(-MainActivity.sToolbarLateralTranslationX,
//            mMainActivityRule.getActivity().getToolbarTheater().getTranslationX(), 0.01);
//        assertEquals(MainActivity.sToolbarLateralTranslationX,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getTranslationX(), 0.01);
//      }
//    });
//    // Photo scene should be displayed
//    assertSceneDisplayed(mPhotoScene, mPhotoScene.getMediaNodes().get(0).getId());
//  }
//
//  private void assertTheater() {
//    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
//    // Assert scale of studio and repertoire buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_STUDIO_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_STUDIO_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarStudio().getScaleY(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_DESELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getScaleY(), 0.01);
//      }
//    });
//    // Assert scale of theater toolbar button
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(MainActivity.TOOLBAR_LATERAL_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleX(), 0.01);
//        assertEquals(MainActivity.TOOLBAR_LATERAL_SELECTED_SCALE,
//            mMainActivityRule.getActivity().getToolbarTheater().getScaleY(), 0.01);
//      }
//    });
//    // Assert translation of lateral toolbar buttons
//    await().untilAsserted(new ThrowingRunnable() {
//      @Override public void run() throws Throwable {
//        assertEquals(-MainActivity.sToolbarLateralTranslationX,
//            mMainActivityRule.getActivity().getToolbarTheater().getTranslationX(), 0.01);
//        assertEquals(MainActivity.sToolbarLateralTranslationX,
//            mMainActivityRule.getActivity().getToolbarRepertoire().getTranslationX(), 0.01);
//      }
//    });
//    // Video scene should be displayed
//    assertSceneDisplayed(mVideoScene, mVideoScene.getMediaNodes().get(0).getId());
//  }
//}