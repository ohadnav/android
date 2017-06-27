package com.truethat.android.empathy;

import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.common.util.BackgroundHandler;
import com.truethat.android.model.Emotion;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 *
 * Executes {@link DetectionTask}s in a sequential manner. The detection is executed in iterations,
 * each started by
 * {@link #next()}, and each has the following stages:
 * 1) an input is requested by {@link ReactionDetectionPubSub#requestInput()}.
 * 2) Once the input received, an {@link AsyncTask} is created.
 * 3) In which, {@link EmotionDetectionClassifier#classify(Image)} is invoked.
 * 4) If the classification yielded an {@link Emotion}, then {@link ReactionDetectionPubSub#onReactionDetected(Emotion)}
 * is called, and otherwise, and new iteration is begun in {@link #next()}.
 */
public class DefaultReactionDetectionModule implements ReactionDetectionModule {
  /**
   * For logging.
   */
  private static final String TAG = "DefaultDetectionModule";
  /**
   * If the much time has passed since an input request and an input was not received, then a new
   * input will be
   * requested.
   */
  @VisibleForTesting static long REQUEST_INPUT_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(3);
  /**
   * Maximum duration in millis to start a new detection attempt, measured with relation to {@code
   * mDetectionStartTime}.
   */
  @VisibleForTesting static long DETECTION_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10);
  private EmotionDetectionClassifier mDetectionClassifier;
  private ReactionDetectionPubSub mDetectionPubSub;
  private DetectionTask mDetectionTask;
  private Date mDetectionStartTime;
  /**
   * Used together with {@code REQUEST_INPUT_TIMEOUT_MILLIS}
   */
  private boolean mInputReceived;
  /**
   * Used to initiate a delayed input request.
   */
  private BackgroundHandler mInputRequestHandler =
      new BackgroundHandler(this.getClass().getSimpleName());

  public DefaultReactionDetectionModule(EmotionDetectionClassifier detectionClassifier) {
    mDetectionClassifier = detectionClassifier;
  }

  @VisibleForTesting static void setDetectionTimeoutMillis(long detectionTimeoutMillis) {
    DETECTION_TIMEOUT_MILLIS = detectionTimeoutMillis;
  }

  @VisibleForTesting static void setRequestInputTimeoutMillis(long requestInputTimeoutMillis) {
    REQUEST_INPUT_TIMEOUT_MILLIS = requestInputTimeoutMillis;
  }

  @Override public void detect(ReactionDetectionPubSub detectionPubSub) {
    // Stop ongoing detection in favor of new one.
    stop();
    mDetectionPubSub = detectionPubSub;
    // Initializes start time and detection task.
    mDetectionStartTime = new Date();
    // Initiates the detection attempts.
    mInputRequestHandler.start();
    next();
  }

  @Override public void attempt(Image image) {
    // Input is received.
    mInputReceived = true;
    if (mDetectionPubSub == null) {
      throw new RuntimeException(
          "Attempting to push input without calling ReactionDetectionModule#classify first.");
    }
    Log.v(TAG, "Starting a new detection attempt.");
    // Concurrent detections are allowed.
    mDetectionTask = new DetectionTask();
    mDetectionTask.execute(image);
  }

  @Override public void stop() {
    Log.v(TAG, "Stopping detection.");
    // Stops the detection task.
    if (mDetectionTask != null) mDetectionTask.cancel(true);
    mInputRequestHandler.stop();
  }

  /**
   * @return whether the current detection task should be stopped (for example, in the case of a
   * timeout).
   */
  private boolean shouldStop() {
    return new Date().getTime() - mDetectionStartTime.getTime() >= DETECTION_TIMEOUT_MILLIS;
  }

  /**
   * Starts a new detection attempt. See the class documentation for a more thorough documentation.
   */
  private void next() {
    if (shouldStop()) {
      Log.v(TAG, "Detection timed out.");
      stop();
    } else {
      Log.v(TAG, "Requesting input.");
      // If remains false, then in REQUEST_INPUT_TIMEOUT_MILLIS we'll request input again.
      mInputReceived = false;
      mDetectionPubSub.requestInput();
      mInputRequestHandler.getHandler().postDelayed(new Runnable() {
        @Override public void run() {
          if (!mInputReceived) next();
        }
      }, REQUEST_INPUT_TIMEOUT_MILLIS);
    }
  }

  /**
   * Classifies images asynchronously. If the classification yielded an {@link Emotion}, then
   * {@link
   * ReactionDetectionPubSub#onReactionDetected(Emotion)} is called, and otherwise, and new
   * iteration is begun in {@link
   * #next()}.
   */
  private class DetectionTask extends AsyncTask<Image, Void, Emotion> {
    @Override protected @Nullable Emotion doInBackground(Image... params) {
      if (params.length == 0) {
        throw new IllegalArgumentException("Must supply an image for detection algorithm");
      }
      return mDetectionClassifier.classify(params[0]);
    }

    @Override protected void onPostExecute(@Nullable Emotion reaction) {
      Log.v(TAG, "Detection attempt completed with " + reaction);
      if (reaction != null) {
        mDetectionPubSub.onReactionDetected(reaction);
        stop();
      } else {
        // Go for next iteration.
        next();
      }
    }
  }
}
