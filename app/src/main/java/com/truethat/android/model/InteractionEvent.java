package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.NetworkUtil;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 * <p>
 * Encapsulates event of user interaction with a reactable.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/InteractionEvent.java</a>
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) public class InteractionEvent {
  private static final long serialVersionUID = 1L;
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
   * <p>
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
  private long mReactableId;

  public InteractionEvent(long userId, long reactableId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction) {
    mTimestamp = timestamp;
    mUserId = userId;
    mReaction = reaction;
    mEventType = eventType;
    mReactableId = reactableId;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }
}
