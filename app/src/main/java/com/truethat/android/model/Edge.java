package com.truethat.android.model;

import java.io.Serializable;

/**
 * Proudly created by ohad on 11/09/2017 for TrueThat.
 * <p>
 * Describes relations between media nodes and the flow in which user will interact with them.
 * {@code <0, 1, HAPPY>} means users that had a {@code HAPPY} reaction to the 0-indexed media node
 * will than view 1-indexed node.
 * <p>
 * Note that we regard the {@link Media} node order in {@link Scene#mMediaNodes} as its index.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Edge.java</a>
 */

public class Edge extends BaseModel implements Serializable {
  private static final long serialVersionUID = 556726533574169005L;
  private Integer mSourceIndex;
  private Integer mTargetIndex;
  private Emotion mReaction;

  public Edge(int sourceIndex, int targetIndex, Emotion reaction) {
    mSourceIndex = sourceIndex;
    mTargetIndex = targetIndex;
    mReaction = reaction;
  }

  public Edge(int sourceIndex, Emotion reaction) {
    mSourceIndex = sourceIndex;
    mReaction = reaction;
  }

  public Emotion getReaction() {
    return mReaction;
  }

  public Integer getTargetIndex() {
    return mTargetIndex;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mSourceIndex != null ? mSourceIndex.hashCode() : 0);
    result = 31 * result + (mTargetIndex != null ? mTargetIndex.hashCode() : 0);
    result = 31 * result + (mReaction != null ? mReaction.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Edge)) return false;
    if (!super.equals(o)) return false;

    Edge edge = (Edge) o;

    if (mSourceIndex != null ? !mSourceIndex.equals(edge.mSourceIndex)
        : edge.mSourceIndex != null) {
      return false;
    }
    if (mTargetIndex != null ? !mTargetIndex.equals(edge.mTargetIndex)
        : edge.mTargetIndex != null) {
      return false;
    }
    return mReaction == edge.mReaction;
  }

  Integer getSourceIndex() {
    return mSourceIndex;
  }
}
