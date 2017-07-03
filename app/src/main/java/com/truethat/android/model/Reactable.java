package com.truethat.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioAPI;
import com.truethat.android.ui.common.media.ReactableFragment;
import com.truethat.android.ui.theater.TheaterActivity;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.TreeMap;
import retrofit2.Call;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * A media item that the user can have an emotional reaction to, such as {@link Scene}.
 * <p>
 * Each implementation should register at {@link NetworkUtil#GSON}.
 */
public abstract class Reactable implements Serializable {
  /**
   * ID as stored in our backend.
   */
  private Long mId;

  /**
   * The user reaction to the reactable, {@code null} for no reaction.
   */
  private Emotion mUserReaction;

  /**
   * Creator of the reactable. By default, the current user is assigned.
   */
  private User mDirector;

  /**
   * Counters of emotional reactions to the reactable, per each emotion.
   */
  private TreeMap<Emotion, Long> mReactionCounters;

  /**
   * Date of creation.
   */
  private Date mCreated;

  /**
   * Whether the reactable was viewed by the user.
   */
  private boolean mViewed;

  Reactable(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction) {
    mId = id;
    mUserReaction = userReaction;
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
  }

  Reactable(User director, Date created) {
    mDirector = director;
    mCreated = created;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Reactable() {
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

  public boolean hasId() {
    return mId != null;
  }

  public boolean isViewed() {
    return mViewed;
  }

  /**
   * Marks this Reactable as viewed by the user.
   */
  public void doView() {
    mViewed = true;
  }

  public User getDirector() {
    return mDirector;
  }

  /**
   * Applies {@code reaction} on the reactable, by updating {@code mReactionCounters} and {@code
   * mUserReaction}.
   *
   * @param reaction of the user reaction.
   */
  public void doReaction(@NonNull Emotion reaction) {
    if (mReactionCounters == null) mReactionCounters = new TreeMap<>();
    increaseReactionCounter(reaction);
    // Check if the user had already reacted this reactable.
    if (mUserReaction != null) {
      decreaseReactionCounter(reaction);
    }
    mUserReaction = reaction;
  }

  public TreeMap<Emotion, Long> getReactionCounters() {
    if (mReactionCounters == null) mReactionCounters = new TreeMap<>();
    return mReactionCounters;
  }

  public Emotion getUserReaction() {
    return mUserReaction;
  }

  /**
   * @return created a {@link ReactableFragment} to show it around in dubious activities such as
   * {@link TheaterActivity}.
   */
  public abstract ReactableFragment createFragment();

  public abstract Call<Reactable> createApiCall(StudioAPI studioAPI);

  /**
   * @return whether the user can react to this reactable.
   */
  public boolean canReactTo() {
    boolean notReactedTo = mUserReaction == null;
    boolean notMine = !App.getAuthModule().getUser().equals(mDirector);
    return notReactedTo && notMine;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reactable)) return false;

    Reactable reactable = (Reactable) o;

    if (!Objects.equals(mId, reactable.mId)) return false;
    if (mViewed != reactable.mViewed) return false;
    if (mUserReaction != reactable.mUserReaction) return false;
    if (mDirector != null ? !mDirector.equals(reactable.mDirector) : reactable.mDirector != null) {
      return false;
    }
    //noinspection SimplifiableIfStatement
    if (mReactionCounters != null ? !mReactionCounters.equals(reactable.mReactionCounters)
        : reactable.mReactionCounters != null) {
      return false;
    }
    return mCreated != null ? mCreated.equals(reactable.mCreated) : reactable.mCreated == null;
  }

  @Override public String toString() {
    return this.getClass().getSimpleName() + "{id: " + mId + "}";
  }

  /**
   * Increases {@code emotion}'s reaction counter in {@code mReactionCounters} by 1. Creates new map
   * key if needed.
   *
   * @param emotion to increase its counter.
   */
  private void increaseReactionCounter(@NonNull Emotion emotion) {
    if (!mReactionCounters.containsKey(emotion)) {
      mReactionCounters.put(emotion, 0L);
    }
    mReactionCounters.put(emotion, mReactionCounters.get(emotion) + 1);
  }

  /**
   * Decreases {@code emotion}'s reaction counter in {@code mReactionCounters} by 1. Deletes the map
   * entry, if the
   * counter reaches 0.
   *
   * @param emotion to decrease its counter.
   */
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
}
