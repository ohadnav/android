package com.truethat.android.empathy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.truethat.android.application.App;
import com.truethat.android.identity.User;
import java.util.Date;
import java.util.TreeMap;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 *
 * An item that the user can have an emotional reaction to, such as scenes and acts.
 */
public abstract class Reactable {
  /**
   * Scene ID, as stored in our backend.
   */
  @SerializedName("id") private long mId;

  /**
   * Whether the user had already reacted to the reactable
   */
  @SerializedName("user_reaction") private Emotion mUserReaction;

  /**
   * Creator of the reactable. By default, the current user is assigned.
   */
  @SerializedName("director") private User mDirector = App.getAuthModule().getCurrentUser();

  /**
   * Counters of emotional reactions to the reactable, per each emotion.
   */
  @SerializedName("reaction_counters") private TreeMap<Emotion, Long> mReactionCounters = new TreeMap<>();

  /**
   * Date of creation.
   */
  @SerializedName("created") private Date mCreated = new Date();

  public Reactable(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction) {
    mId = id;
    mUserReaction = userReaction;
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
  }

  public Date getCreated() {
    return mCreated;
  }

  public long getId() {
    return mId;
  }

  public void setId(long id) {
    mId = id;
  }

  public User getDirector() {
    return mDirector;
  }

  public void doReaction(@NonNull Emotion emotion) {
    increaseReactionCounter(emotion);
    // Check if the user had already reacted this reactable.
    if (mUserReaction != null) {
      decreaseReactionCounter(emotion);
    }
    mUserReaction = emotion;
  }

  public TreeMap<Emotion, Long> getReactionCounters() {
    return mReactionCounters;
  }

  public Emotion getUserReaction() {
    return mUserReaction;
  }

  private void increaseReactionCounter(@NonNull Emotion emotion) {
    if (!mReactionCounters.containsKey(emotion)) {
      mReactionCounters.put(emotion, 0L);
    }
    mReactionCounters.put(emotion, mReactionCounters.get(emotion) + 1);
  }

  private void decreaseReactionCounter(@NonNull Emotion emotion) {
    if (!mReactionCounters.containsKey(emotion)) {
      throw new IllegalArgumentException(
          this.getClass().getSimpleName() + " was never reacted with " + emotion.name() + ".");
    } else if (mReactionCounters.get(emotion) <= 1) {
      mReactionCounters.remove(emotion);
    } else {
      mReactionCounters.put(emotion, mReactionCounters.get(emotion) - 1);
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Reactable reactable = (Reactable) o;

    return mId == reactable.mId;
  }

  @Override public int hashCode() {
    return (int) (mId ^ (mId >>> 32));
  }
}
