package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.model.Emotion;
import java.util.Date;
import org.awaitility.core.ThrowingRunnable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */
public class DefaultReactionDetectionModuleTest extends BaseApplicationTestSuite {
  private static final long REQUEST_INPUT_TIMEOUT_MILLIS =
      DefaultReactionDetectionModule.REQUEST_INPUT_TIMEOUT_MILLIS;
  private static final long DETECTION_TIMEOUT_MILLIS =
      DefaultReactionDetectionModule.DETECTION_TIMEOUT_MILLIS;
  private static final long TEST_TIMEOUT_MILLIS = 100;
  private Emotion mDetectedReaction;
  private ReactionDetectionModule mDetectionModule;
  private ReactionDetectionPubSub mDetectionPubSub;
  private boolean mFirstInputRequest;

  @BeforeClass public static void beforeClass() throws Exception {
    DefaultReactionDetectionModule.setDetectionTimeoutMillis(TEST_TIMEOUT_MILLIS);
    DefaultReactionDetectionModule.setRequestInputTimeoutMillis(TEST_TIMEOUT_MILLIS / 2);
  }

  @AfterClass public static void afterClass() throws Exception {
    DefaultReactionDetectionModule.setDetectionTimeoutMillis(DETECTION_TIMEOUT_MILLIS);
    DefaultReactionDetectionModule.setRequestInputTimeoutMillis(REQUEST_INPUT_TIMEOUT_MILLIS);
  }

  @Before public void setUp() throws Exception {
    super.setUp();
    // Resets the mDetectedReaction emotion.
    mDetectedReaction = null;
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

  @Test public void detectionTimedOut() throws Exception {
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return null;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    Thread.sleep(TEST_TIMEOUT_MILLIS * 2);
    assertNull(mDetectedReaction);
  }

  @Test public void inputRequestedAgain() throws Exception {
    final Emotion expected = Emotion.HAPPY;
    mFirstInputRequest = true;
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return expected;
      }
    });
    mDetectionModule.detect(new ReactionDetectionPubSub() {
      @Override public void onReactionDetected(Emotion reaction) {
        mDetectedReaction = reaction;
      }

      @Override public void requestInput() {
        if (mFirstInputRequest) {
          mFirstInputRequest = false;
        } else {
          //noinspection ConstantConditions
          mDetectionModule.attempt(null);
        }
      }
    });
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(expected, mDetectedReaction);
      }
    });
  }

  @Test public void stop() throws Exception {
    final Date now = new Date();
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        return new Date().getTime() - now.getTime() > TEST_TIMEOUT_MILLIS / 2 ? Emotion.HAPPY
            : null;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    mDetectionModule.stop();
    Thread.sleep(TEST_TIMEOUT_MILLIS * 2);
    assertNull(mDetectedReaction);
  }

  // Should stop ongoing detection in favor of new one.
  @Test public void concurrentDetections() throws Exception {
    mFirstInputRequest = true;
    final Emotion expected = Emotion.HAPPY;
    final Emotion unexpected = Emotion.SAD;
    mDetectionModule = new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
      @Nullable @Override public Emotion classify(Image image) {
        Emotion reaction = unexpected;
        if (mFirstInputRequest) {
          mFirstInputRequest = false;
          try {
            Thread.sleep(TEST_TIMEOUT_MILLIS / 2);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          reaction = expected;
        }
        return reaction;
      }
    });
    mDetectionModule.detect(mDetectionPubSub);
    Thread.sleep(TEST_TIMEOUT_MILLIS / 10);
    // Start a new detection
    mDetectionModule.detect(mDetectionPubSub);
    // Let the first detection apparently finish.
    Thread.sleep(TEST_TIMEOUT_MILLIS / 2);
    assertEquals(expected, mDetectedReaction);
  }
}