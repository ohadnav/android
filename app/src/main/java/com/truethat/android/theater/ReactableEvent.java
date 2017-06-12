package com.truethat.android.theater;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.EventType;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.Reactable;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 *
 * Encapsulates event of user interaction with a reactable.
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) class ReactableEvent {
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

  ReactableEvent(long userId, long sceneId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction) {
    mTimestamp = timestamp;
    mUserId = userId;
    mReaction = reaction;
    mEventType = eventType;
    mSceneId = sceneId;
  }
}
