package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.empathy.Emotion;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 *
 * Encapsulates event of user interaction with a reactable.
 *
 * @backend <a>https://goo.gl/s9o2rt</a>
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) public class ReactableEvent {
  /**
   * Client UTC timestamp
   */
  private Date mTimestamp;

  /**
   * ID of the user that triggered the event.
   */
  private long mUserId;

  /**
   * For {@link EventType#REACTABLE_REACTION}.
   *
   * Must be null for irrelevant events (such as {@link EventType#REACTABLE_VIEW}).
   */
  private Emotion mReaction;

  /**
   * Event type, to sync with frontend clients.
   */
  private EventType mEventType;

  /**
   * Of the {@link Reactable} that was interacted with.
   */
  private long mSceneId;

  public ReactableEvent(long userId, long sceneId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction) {
    mTimestamp = timestamp;
    mUserId = userId;
    mReaction = reaction;
    mEventType = eventType;
    mSceneId = sceneId;
  }
}