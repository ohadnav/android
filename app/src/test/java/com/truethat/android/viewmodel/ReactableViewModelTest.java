package com.truethat.android.viewmodel;

import android.support.annotation.NonNull;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.di.component.DaggerReactableInjectorComponent;
import com.truethat.android.di.module.ReactableModule;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.ReatablesPagerViewInterface;
import java.util.Collections;
import java.util.TreeMap;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */
public class ReactableViewModelTest extends ViewModelTestSuite {
  private static final long REACTABLE_ID = 1;
  private static final String IMAGE_URL = "http://www.ishim.co.il/i/11/1139.jpg";
  private static final User DIRECTOR = new User(1L, "Tomer", "Sh");
  private static final TreeMap<Emotion, Long> REACTION_COUNTERS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, 100L);
    put(Emotion.SAD, 20000L);
  }};
  private static final Emotion REACTION = Emotion.HAPPY;
  private static final Emotion REACTION_2 = Emotion.SAD;
  private ReactableViewModel<Reactable> mViewModel = new ReactableViewModel<>();
  private Scene mScene;

  private static void assertReactableDisplayed(ReactableViewModel viewModel, Reactable reactable,
      @NonNull User currentUser) {
    assertEquals(reactable.getId(), viewModel.getReactable().getId());
    if (reactable.getUserReaction() != null) {
      // Display user reaction when possible.
      assertEquals(reactable.getUserReaction().getDrawableResource(),
          viewModel.mReactionDrawableResource.get());
    } else {
      // Display common reaction otherwise.
      assertEquals(reactable.getReactionCounters().lastKey().getDrawableResource(),
          viewModel.mReactionDrawableResource.get());
    }
    // Counters should be summed up and abbreviated.
    assertEquals(NumberUtil.format(NumberUtil.sum(reactable.getReactionCounters())),
        viewModel.mReactionsCountText.get());
    // Hide director name when ths current user is the director.
    assertNotEquals(currentUser.equals(reactable.getDirector()),
        viewModel.mDirectorNameVisibility.get());
    assertEquals(reactable.getDirector().getDisplayName(), viewModel.mDirectorName.get());
    // Display time ago in a proper fashion.
    assertEquals(DateUtil.formatTimeAgo(reactable.getCreated()), viewModel.mTimeAgoText.get());
  }

  @Test public void properDisplay() throws Exception {
    mScene = new Scene(REACTABLE_ID, IMAGE_URL, DIRECTOR, REACTION_COUNTERS, mNow, REACTION);
    initReactableViewModel();
    assertReactableDisplayed(mViewModel, mScene, mViewModel.getCurrentUser().get());
  }

  @Test public void doView() throws Exception {
    mScene = new Scene(REACTABLE_ID, IMAGE_URL, DIRECTOR, REACTION_COUNTERS, mNow, null);
    initReactableViewModel();
    // Assert a view event is sent
    final int currentRequestCount = mMockWebServer.getRequestCount();
    mViewModel.onDisplay();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(currentRequestCount + 1, mMockWebServer.getRequestCount());
      }
    });
  }

  @Test public void reactionDetection() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(REACTABLE_ID, IMAGE_URL, DIRECTOR, reactionCounters, mNow, null);
    initReactableViewModel();
    final int currentRequestCount = mMockWebServer.getRequestCount();
    mViewModel.onDisplay();
    // Applies the detection.
    mFakeReactionDetectionManager.doDetection(REACTION);
    // Assert a reaction and view events are sent
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(currentRequestCount + 2, mMockWebServer.getRequestCount());
      }
    });
    // The detected reaction should be registered
    assertEquals(REACTION, mViewModel.getReactable().getUserReaction());
    // and displayed
    assertEquals(REACTION.getDrawableResource(), mViewModel.mReactionDrawableResource.get());
    assertEquals("2", mViewModel.mReactionsCountText.get());
  }

  @Test public void reactionNotDetectedWhenDirectorIsCurrentUser() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene =
        new Scene(REACTABLE_ID, IMAGE_URL, mFakeAuthManager.currentUser(), reactionCounters, mNow,
            null);
    initReactableViewModel();
    mViewModel.onDisplay();
    // Should not be subscribed to reaction detection.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Fake a detection.
    mFakeReactionDetectionManager.doDetection(REACTION);
    // Should not have an effect
    assertNull(mViewModel.getReactable().getUserReaction());
    assertEquals(reactionCounters, mViewModel.getReactable().getReactionCounters());
  }

  @Test public void reactionNotDetectedOnHidden() throws Exception {
    mScene = new Scene(REACTABLE_ID, IMAGE_URL, DIRECTOR, REACTION_COUNTERS, mNow, null);
    initReactableViewModel();
    mViewModel.onDisplay();
    // Hides view.
    mViewModel.onHidden();
    // Do a detection
    mFakeReactionDetectionManager.doDetection(REACTION);
    // Should not register the detection.
    assertNull(mViewModel.getReactable().getUserReaction());
  }

  @Test public void reactionCanNotBeDetectedTwice() throws Exception {
    mScene = new Scene(REACTABLE_ID, IMAGE_URL, DIRECTOR, REACTION_COUNTERS, mNow, REACTION);
    initReactableViewModel();
    mViewModel.onDisplay();
    // Do a detection
    mFakeReactionDetectionManager.doDetection(REACTION_2);
    // Should not register the detection.
    assertEquals(REACTION, mViewModel.getReactable().getUserReaction());
  }

  private void initReactableViewModel() throws Exception {
    mViewModel = createViewModel(mViewModel.getClass(), new UnitTestViewInterface());
    DaggerReactableInjectorComponent.builder()
        .appComponent(mUnitTestComponent)
        .reactableModule(new ReactableModule(mScene))
        .build()
        .inject(mViewModel);
    mViewModel.getReactionDetectionManager().start();
    mViewModel.onStart();
  }
}