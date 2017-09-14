package com.truethat.android.model;

import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 * <p>
 * Encapsulates event of user interaction with a scene.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/InteractionEvent.java</a>
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) public class InteractionEvent extends BaseModel
    implements Serializable {
  private static final long serialVersionUID = -3002722340976455252L;
  /**
   * Client UTC timestamp
   */
  private Date mTimestamp;
  /**
   * ID of the user that triggered the event.
   */
  private Long mUserId;
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
  private Long mSceneId;
  /**
   * The {@link Media} index within {@link Scene#mMediaNodes}.
   */
  private Long mMediaIndex;

  public InteractionEvent(Long userId, Long sceneId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction, Long mediaIndex) {
    mTimestamp = timestamp;
    mUserId = userId;
    mReaction = reaction;
    mEventType = eventType;
    mSceneId = sceneId;
    mMediaIndex = mediaIndex;
  }

  public Long getUserId() {
    return mUserId;
  }

  public EventType getEventType() {
    return mEventType;
  }

  public Long getSceneId() {
    return mSceneId;
  }

  public Long getMediaIndex() {
    return mMediaIndex;
  }

  public Emotion getReaction() {
    return mReaction;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mTimestamp != null ? mTimestamp.hashCode() : 0);
    result = 31 * result + (mUserId != null ? mUserId.hashCode() : 0);
    result = 31 * result + (mReaction != null ? mReaction.hashCode() : 0);
    result = 31 * result + (mEventType != null ? mEventType.hashCode() : 0);
    result = 31 * result + (mSceneId != null ? mSceneId.hashCode() : 0);
    result = 31 * result + (mMediaIndex != null ? mMediaIndex.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InteractionEvent)) return false;
    if (!super.equals(o)) return false;

    InteractionEvent that = (InteractionEvent) o;

    if (mTimestamp != null ? !mTimestamp.equals(that.mTimestamp) : that.mTimestamp != null) {
      return false;
    }
    if (mUserId != null ? !mUserId.equals(that.mUserId) : that.mUserId != null) return false;
    if (mReaction != that.mReaction) return false;
    if (mEventType != that.mEventType) return false;
    if (mSceneId != null ? !mSceneId.equals(that.mSceneId) : that.mSceneId != null) return false;
    return mMediaIndex != null ? mMediaIndex.equals(that.mMediaIndex) : that.mMediaIndex == null;
  }
}
