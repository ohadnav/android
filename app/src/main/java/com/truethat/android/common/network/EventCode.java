package com.truethat.android.common.network;

/**
 * Proudly created by ohad on 07/06/2017 for TrueThat.
 *
 * EventCodes of our deal users and magical application. Each event code has an integer code, which should be aligned
 * with our backend.
 */
public enum EventCode {
  /**
   * Reflects that a scene was displayed to the user.
   */
  SCENE_VIEW(100),

  /**
   * Reflects a user emotional reaction to a scene.
   */
  SCENE_REACTION(101);

  private int mCode;

  EventCode(int code) {
    mCode = code;
  }
}
