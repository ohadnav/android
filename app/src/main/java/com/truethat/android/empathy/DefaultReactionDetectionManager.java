package com.truethat.android.empathy;

import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.view.activity.BaseActivity;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * A wrapper for Affectiva emotion detection engine.
 */
public class DefaultReactionDetectionManager extends BaseReactionDetectionManager {
  private Context mContext;
  private HandlerThread detectionThread;
  private DetectionHandler detectionHandler;

  public DefaultReactionDetectionManager(Context context) {
    mContext = context;
  }

  @Override public void start(BaseActivity activity) {
    super.start(activity);
    activity.getPermissionsManager().requestIfNeeded(activity, Permission.CAMERA);
    if (!activity.getPermissionsManager().isPermissionGranted(Permission.CAMERA)) {
      Log.w(TAG, "Started without camera permissions.");
      stop();
    } else if (detectionThread == null) {
      // fire up the background thread
      detectionThread = new DetectionThread();
      detectionThread.start();
      detectionHandler = new DetectionHandler(mContext, detectionThread, this);
      detectionHandler.sendStartMessage();
    }
  }

  @Override public void stop() {
    super.stop();
    if (detectionHandler != null) {
      detectionHandler.sendStopMessage();
      try {
        detectionThread.join();
        detectionThread = null;
        detectionHandler = null;
      } catch (InterruptedException ignored) {
      }
    }
  }

  private static class DetectionThread extends HandlerThread {
    private DetectionThread() {
      super("ReactionDetectionThread");
    }
  }
}
