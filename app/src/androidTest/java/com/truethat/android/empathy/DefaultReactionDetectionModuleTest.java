package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.common.TestActivity;
import java.util.Date;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */
public class DefaultReactionDetectionModuleTest {
  private static long DETECTION_TIMEOUT_MILLIS = 100;
  @Rule public ActivityTestRule<TestActivity> mTestActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  private Emotion mDetectedReaction;
  private ReactionDetectionModule mDetectionModule;
  private ReactionDetectionPubSub mDetectionPubSub;

  @BeforeClass public static void beforeClass() throws Exception {
    DefaultReactionDetectionModule.setDetectionTimeoutMillis(DETECTION_TIMEOUT_MILLIS);
  }

  @Before public void setUp() throws Exception {
    // Resets the mDetectedReaction emotion.
    mDetectedReaction = null;
    // Initialize Awaitility
    Awaitility.reset();
    mDetectionPubSub = new ReactionDetectionPubSub() {
      @Override public void onReactionDetected(Emotion reaction) {
        mDetectedReaction = reaction;
      }

      @Override public void requestInput() {
        //noinspection ConstantConditions
        mDetectionModule.attempt(null);
      }
    };
  }

  @Test public void emotionDetected() throws Exception {
    final Emotion expected = Emotion.HAPPY;
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return expected;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(expected, mDetectedReaction);
      }
    });
  }

  @Test public void emotionDetected_onSecondAttempt() throws Exception {
    final Emotion expected = Emotion.HAPPY;
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      boolean isFirst = true;

      @Nullable @Override public Emotion classify(Image image) {
        Emotion reaction = null;
        if (!isFirst) reaction = expected;
        isFirst = false;
        return reaction;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(expected, mDetectedReaction);
      }
    });
  }

  @Test public void emotionNotDetected() throws Exception {
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return null;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    Thread.sleep(DETECTION_TIMEOUT_MILLIS * 2);
    assertNull(mDetectedReaction);
  }

  @Test public void stop() throws Exception {
    final Date now = new Date();
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return new Date().getTime() - now.getTime() > DETECTION_TIMEOUT_MILLIS / 2 ? Emotion.HAPPY : null;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    mDetectionModule.stop();
    Thread.sleep(DETECTION_TIMEOUT_MILLIS * 2);
    assertNull(mDetectedReaction);
  }
}