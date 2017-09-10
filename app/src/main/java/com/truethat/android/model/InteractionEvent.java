package com.truethat.android.model;

import android.support.annotation.Nullable;
import com.truethat.android.common.network.NetworkUtil;
import java.io.Serializable;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 * <p>
 * Encapsulates event of user interaction with a scene.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/InteractionEvent.java</a>
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) public class InteractionEvent
    implements Serializable {
  private static final long serialVersionUID = 3577142099634828092L;
  /**
   * Client UTC timestamp
   */
  private Date mTimestamp;
  /**
   * ID of the user that triggered the event.
   */
  private long mUserId;
  /**
   * For {@link EventType#REACTION}.
   * <p>
   * Must be null for irrelevant events (such as {@link EventType#VIEW}).
   */
  private Emotion mReaction;
  /**
   * Event type, to sync with frontend clients.
   */
  private EventType mEventType;
  /**
   * Of the {@link Scene} that was interacted with.
   */
  private long mSceneId;

  public InteractionEvent(long userId, long sceneId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction) {
    mTimestamp = timestamp;
    mUserId = userId;
    mReaction = reaction;
    mEventType = eventType;
    mSceneId = sceneId;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }
}
