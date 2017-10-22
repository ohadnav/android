package com.truethat.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import java.util.Date;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 * <p>
 * Encapsulates event of user interaction with a scene.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/InteractionEvent.java</a>
 */

@SuppressWarnings({ "unused", "FieldCanBeLocal" }) public class InteractionEvent extends BaseModel {
  public static final Parcelable.Creator<InteractionEvent> CREATOR =
      new Parcelable.Creator<InteractionEvent>() {
        @Override public InteractionEvent createFromParcel(Parcel source) {
          return new InteractionEvent(source);
        }

        @Override public InteractionEvent[] newArray(int size) {
          return new InteractionEvent[size];
        }
      };
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
  private Long mMediaId;

  public InteractionEvent(Long userId, Long sceneId, Date timestamp, EventType eventType,
      @Nullable Emotion reaction, Long mediaId) {
    mUserId = userId;
    mSceneId = sceneId;
    mTimestamp = timestamp;
    mEventType = eventType;
    mReaction = reaction;
    mMediaId = mediaId;
  }

  private InteractionEvent(Parcel in) {
    super(in);
    mUserId = (Long) in.readValue(Long.class.getClassLoader());
    mSceneId = (Long) in.readValue(Long.class.getClassLoader());
    mTimestamp = (Date) in.readValue(Date.class.getClassLoader());
    mEventType = (EventType) in.readValue(EventType.class.getClassLoader());
    mReaction = (Emotion) in.readValue(Emotion.class.getClassLoader());
    mMediaId = (Long) in.readValue(Long.class.getClassLoader());
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeValue(mUserId);
    dest.writeValue(mSceneId);
    dest.writeValue(mTimestamp);
    dest.writeValue(mEventType);
    dest.writeValue(mReaction);
    dest.writeValue(mMediaId);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mTimestamp != null ? mTimestamp.hashCode() : 0);
    result = 31 * result + (mUserId != null ? mUserId.hashCode() : 0);
    result = 31 * result + (mReaction != null ? mReaction.hashCode() : 0);
    result = 31 * result + (mEventType != null ? mEventType.hashCode() : 0);
    result = 31 * result + (mSceneId != null ? mSceneId.hashCode() : 0);
    result = 31 * result + (mMediaId != null ? mMediaId.hashCode() : 0);
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
    return mMediaId != null ? mMediaId.equals(that.mMediaId) : that.mMediaId == null;
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

  public Long getMediaId() {
    return mMediaId;
  }

  public Emotion getReaction() {
    return mReaction;
  }
}
