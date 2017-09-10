package com.truethat.android.application;

/**
 * Proudly created by ohad on 04/09/2017 for TrueThat.
 */

public enum LoggingKey {
  // -------------------- UI + UX -----------------------
  /**
   * Currently displayed scene.
   */
  DISPLAYED_SCENE, /**
   * Last directed scene.
   */
  DIRECTED_SCENE, /**
   * Current resumed activity.
   */
  ACTIVITY, /**
   * Last user interaction with a scene.
   */
  LAST_INTERACTION_EVENT,
  // --------------------- Network ----------------------
  /**
   * User used for auth request.
   */
  AUTH_USER, /**
   * Last network request URL.
   */
  LAST_NETWORK_REQUEST
}
