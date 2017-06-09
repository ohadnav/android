package com.truethat.android.empathy;

import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */

public class DefaultReactionDetectionModule implements ReactionDetectionModule {
  private static long MAX_DETECTION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);

  private EmotionDetectionAlgorithm mDetectionAlgorithm;
  private ReactionDetectionPubSub mDetectionPubSub;
  private DetectionTask mDetectionTask;
  private Date mStartTime;
  private Queue<Image> mInputQueue = new LinkedList<>();

  public DefaultReactionDetectionModule(EmotionDetectionAlgorithm detectionAlgorithm) {
    mDetectionAlgorithm = detectionAlgorithm;
  }

  @VisibleForTesting static void setMaxDetectionTimeMillis(long maxDetectionTimeMillis) {
    MAX_DETECTION_TIME_MILLIS = maxDetectionTimeMillis;
  }

  @Override public void detect(ReactionDetectionPubSub detectionPubSub) {
    mDetectionPubSub = detectionPubSub;
    // Initializes start time and detection task.
    mStartTime = new Date();
    mDetectionTask = new DetectionTask();
    // Initiates the detection attempts.
    detectionPubSub.requestInput();
  }

  @Override public void pushInput(Image image) {
    if (mDetectionTask == null) {
      throw new RuntimeException("Attempting to push input without calling ReactionDetectionModule#detect first.");
    }
    // Pushing image to input queue.
    mInputQueue.add(image);
    // Concurrent detections are allowed.
    mDetectionTask = new DetectionTask();
    mDetectionTask.execute(mInputQueue.poll());
  }

  @Override public void stop() {
    // Stops detection task
    if (mDetectionTask != null) mDetectionTask.cancel(true);
  }

  private class DetectionTask extends AsyncTask<Image, Void, Emotion> {
    @Override protected @Nullable Emotion doInBackground(Image... params) {
      if (params.length == 0) {
        throw new IllegalArgumentException("Must supply image for detection algorithm");
      }
      return mDetectionAlgorithm.detect(params[0]);
    }

    @Override protected void onPostExecute(@Nullable Emotion reaction) {
      if (reaction != null) {
        mDetectionPubSub.onReactionDetected(reaction);
      } else {
        // Stop condition, i.e. reaction detection is taking too long.
        if (new Date().getTime() - mStartTime.getTime() < MAX_DETECTION_TIME_MILLIS) {
          // Request input is expected to trigger pushInput.
          mDetectionPubSub.requestInput();
        }
      }
    }
  }
}
