package com.truethat.android.common.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 * <p>
 * Background thread wrapper.
 */

public class BackgroundHandler {
  private HandlerThread mThread;
  private Handler mHandler;
  private String mName;

  public BackgroundHandler(String name) {
    mName = name;
  }

  public Handler getHandler() {
    return mHandler;
  }

  /**
   * Must be called in order to use {@link #mHandler}.
   */
  public void start() {
    if (mThread == null) {
      mThread = new HandlerThread(mName);
      mThread.start();
      mHandler = new Handler(mThread.getLooper());
    }
  }

  /**
   * Stops the handler in a peaceful manner.
   */
  public void stop() {
    if (mThread != null) {
      mThread.quitSafely();
      try {
        mThread.join();
        mThread = null;
        mHandler = null;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
