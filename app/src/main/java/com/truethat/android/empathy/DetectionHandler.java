package com.truethat.android.empathy;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.FrameDetector;
import com.truethat.android.model.Emotion;
import java.util.Date;
import java.util.List;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 * <p>
 * A handler for the DetectionThread.
 */
class DetectionHandler extends Handler {
  private static final double DETECTION_THRESHOLD = 0.2;
  //Incoming message codes
  private static final int START = 0;
  private static final int STOP = 1;
  private String TAG = this.getClass().getSimpleName();
  private CameraHelper mCameraHelper;
  private FrameDetector mFrameDetector;
  private SurfaceTexture mSurfaceTexture;

  DetectionHandler(Context context, HandlerThread detectionThread,
      final BaseReactionDetectionManager reactionDetectionManager) {
    // note: getLooper will block until the the thread's looper has been prepared
    super(detectionThread.getLooper());

    Display display =
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    mCameraHelper = new CameraHelper(context, display, new DetectionHandler.CameraHelperListener());
    mSurfaceTexture = new SurfaceTexture(0); // a dummy texture

    // Set up the FrameDetector.  For the purposes of this sample app, we'll just request
    // listen for face events and request valence scores.
    mFrameDetector = new FrameDetector(context);
    mFrameDetector.setDetectAllEmotions(true);
    mFrameDetector.setImageListener(new Detector.ImageListener() {
      @Override public void onImageResults(List<Face> faces, Frame frame, float v) {
        for (Face face : faces) {
          if (face.emotions.getJoy() > DETECTION_THRESHOLD) {
            reactionDetectionManager.onReactionDetected(Emotion.HAPPY);
          } else if (face.emotions.getSadness() > DETECTION_THRESHOLD) {
            reactionDetectionManager.onReactionDetected(Emotion.SAD);
          }
        }
      }
    });
  }

  /**
   * Process incoming messages
   *
   * @param msg message to handle
   */
  @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case START:
        Log.d(TAG, "starting background processing of frames");
        try {
          mFrameDetector.start();
          //noinspection deprecation
          mCameraHelper.acquire(Camera.CameraInfo.CAMERA_FACING_FRONT);
          mCameraHelper.start(mSurfaceTexture); // initiates previewing
        } catch (IllegalStateException e) {
          Log.d(TAG, "couldn't open camera: " + e.getMessage());
          return;
        }
        break;
      case STOP:
        Log.d(TAG, "stopping background processing of frames");
        mCameraHelper.stop(); // stops previewing
        mCameraHelper.release();
        mFrameDetector.stop();

        Log.d(TAG, "quitting detection thread");
        ((HandlerThread) getLooper().getThread()).quit();
        break;

      default:
        break;
    }
  }

  /**
   * asynchronously start processing on the background thread
   */
  void sendStartMessage() {
    sendMessage(obtainMessage(START));
  }

  /**
   * asynchronously stop processing on the background thread
   */
  void sendStopMessage() {
    sendMessage(obtainMessage(STOP));
  }

  /**
   * A mListener for CameraHelper callbacks
   */
  private class CameraHelperListener implements CameraHelper.Listener {
    private static final long TIMESTAMP_DELTA_MILLIS = 100;
    private Date lastTimestamp = new Date();
    private float detectionTimestamp = 0;

    @Override
    public void onFrameAvailable(byte[] frame, int width, int height, Frame.ROTATE rotation) {
      Log.d(TAG, "received frame");
      Date timeStamp = new Date();
      if (timeStamp.getTime() > lastTimestamp.getTime() + TIMESTAMP_DELTA_MILLIS) {
        lastTimestamp = timeStamp;
        detectionTimestamp += 0.1;
        mFrameDetector.process(createFrameFromData(frame, width, height, rotation),
            detectionTimestamp);
      }
    }

    @Override public void onFrameSizeSelected(int width, int height, Frame.ROTATE rotation) {
    }

    private Frame createFrameFromData(byte[] frameData, int width, int height,
        Frame.ROTATE rotation) {
      Frame.ByteArrayFrame frame =
          new Frame.ByteArrayFrame(frameData, width, height, Frame.COLOR_FORMAT.YUV_NV21);
      frame.setTargetRotation(rotation);
      return frame;
    }
  }
}
