package com.truethat.android.model;

import java.io.Serializable;

/**
 * Proudly created by ohad on 11/09/2017 for TrueThat.
 * <p>
 * Describes relations between media nodes and the flow in which user will interact with them.
 * {@code <0, 1, HAPPY>} means users that had a {@code HAPPY} reaction to the 0-indexed media node
 * will than view 1-indexed node.
 * <p>
 * Note that {@link #mSourceId} and {@link #mTargetId} regard {@link Media#mId}.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Edge.java</a>
 */

public class Edge extends BaseModel implements Serializable {
  private static final long serialVersionUID = -2300569022542504631L;
  /**
   * Media ID of edge source.
   */
  private Long mSourceId;
  /**
   * Media ID of edge target.
   */
  private Long mTargetId;
  /**
   * Type of link between the two nodes. i.e. which reaction should trigger move from source to
   * target.
   */
  private Emotion mReaction;

  public Edge(Long sourceId, Long targetId, Emotion reaction) {
    mSourceId = sourceId;
    mTargetId = targetId;
    mReaction = reaction;
  }

  public Edge(Long sourceId, Emotion reaction) {
    mSourceId = sourceId;
    mReaction = reaction;
  }

  public Emotion getReaction() {
    return mReaction;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mSourceId != null ? mSourceId.hashCode() : 0);
    result = 31 * result + (mTargetId != null ? mTargetId.hashCode() : 0);
    result = 31 * result + (mReaction != null ? mReaction.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Edge)) return false;
    if (!super.equals(o)) return false;

    Edge edge = (Edge) o;

    if (mSourceId != null ? !mSourceId.equals(edge.mSourceId) : edge.mSourceId != null) {
      return false;
    }
    if (mTargetId != null ? !mTargetId.equals(edge.mTargetId) : edge.mTargetId != null) {
      return false;
    }
    return mReaction == edge.mReaction;
  }

  Long getTargetId() {
    return mTargetId;
  }

  Long getSourceId() {
    return mSourceId;
  }
}
