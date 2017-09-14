package com.truethat.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioApi;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import okhttp3.MultipartBody;
import retrofit2.Call;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * A media item that the user can have an emotional reaction to.
 */
public class Scene implements Serializable {
  private static final long serialVersionUID = 7734272873629700816L;
  /**
   * ID as stored in our backend.
   */
  private Long mId;
  /**
   * Creator of the scene. By default, the current user is assigned.
   */
  private User mDirector;
  /**
   * Counters of emotional reactions to the scene, per each emotion.
   */
  private TreeMap<Emotion, Long> mReactionCounters;
  /**
   * Date of creation.
   */
  private Date mCreated;
  /**
   * The media associated with this scene, such as a {@link Photo}.
   */
  private List<Media> mMediaNodes;
  /**
   * The interaction flow of users with this scene.
   */
  private List<Edge> mEdges;
  /**
   * The flow of the user interaction with this scene. Each node represents a media item such as
   * video or a photo and each edge describe which reaction leads from one media item to the next.
   */
  private transient FlowTree mFlowTree;

  @VisibleForTesting
  public Scene(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      Media rootMedia) {
    mId = id;
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
    mMediaNodes = Collections.singletonList(rootMedia);
  }

  @VisibleForTesting
  public Scene(Long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      List<Media> mediaNodes, List<Edge> edges) {
    mId = id;
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
    mMediaNodes = mediaNodes;
    mEdges = edges;
  }

  public Scene(Media media) {
    mDirector = AppContainer.getAuthManager().getCurrentUser();
    mCreated = new Date();
    mMediaNodes = new ArrayList<>();
    mMediaNodes.add(media);
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Scene() {
  }

  public List<Media> getMediaNodes() {
    return mMediaNodes;
  }

  /**
   * Adds the new media to {@link #mFlowTree}.
   *
   * @param newMedia    to add
   * @param partialEdge to deduce source index and reaction from.
   */
  public void addMedia(Media newMedia, Edge partialEdge) {
    mMediaNodes.add(newMedia);
    Edge newEdge = new Edge(partialEdge.getSourceIndex(), mMediaNodes.indexOf(newMedia),
        partialEdge.getReaction());
    if (mEdges == null) {
      mEdges = new ArrayList<>();
    }
    mEdges.add(newEdge);
    addEdgeToTree(newEdge);
  }

  @Nullable public Media getNextMedia(Media current, Emotion reaction) {
    if (!getFlowTree().getNodes().containsKey(current)) {
      throw new IllegalArgumentException("Flow tree has no node with media " + current);
    }
    if (!getFlowTree().getNodes().get(current).getChildren().containsKey(reaction)) {
      return null;
    }
    return getFlowTree().getNodes().get(current).getChildren().get(reaction).getMedia();
  }

  public void removeMedia(Media media) {
    for (FlowTree.Node node : getFlowTree().getNodes().get(media).getChildren().values()) {
      removeMedia(node.getMedia());
    }
    getFlowTree().remove(media);
    mMediaNodes.remove(media);
  }

  public Media getRootMediaNode() {
    return getFlowTree().getRoot().getMedia();
  }

  public Date getCreated() {
    return mCreated;
  }

  public Long getId() {
    return mId;
  }

  @SuppressWarnings("SameParameterValue") @VisibleForTesting public void setId(Long id) {
    mId = id;
  }

  public User getDirector() {
    return mDirector;
  }

  /**
   * Applies {@code reaction} on the scene, by updating {@code mReactionCounters} and {@code
   * mUserReaction}.
   *
   * @param reaction of the user reaction.
   */
  public void doReaction(@NonNull Emotion reaction) {
    if (mReactionCounters == null) mReactionCounters = new TreeMap<>();
    increaseReactionCounter(reaction);
  }

  public TreeMap<Emotion, Long> getReactionCounters() {
    if (mReactionCounters == null) mReactionCounters = new TreeMap<>();
    return mReactionCounters;
  }

  @SuppressWarnings("unchecked") public Call<Scene> createApiCall() {
    List<MultipartBody.Part> mediaParts = new ArrayList<>();
    for (int i = 0; i < mMediaNodes.size(); i++) {
      mediaParts.add(mMediaNodes.get(i).createPart(generatePartName(i)));
    }
    MultipartBody.Part scenePart =
        MultipartBody.Part.createFormData(StudioApi.SCENE_PART, NetworkUtil.GSON.toJson(this));
    return NetworkUtil.createApi(StudioApi.class).saveScene(scenePart, mediaParts);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;

    Scene scene = (Scene) o;

    if (!Objects.equals(mId, scene.mId)) return false;
    if (mDirector != null ? !mDirector.equals(scene.mDirector) : scene.mDirector != null) {
      return false;
    }
    //noinspection SimplifiableIfStatement
    if (mReactionCounters != null ? !mReactionCounters.equals(scene.mReactionCounters)
        : scene.mReactionCounters != null) {
      return false;
    }
    return mCreated != null ? mCreated.equals(scene.mCreated) : scene.mCreated == null;
  }

  @Override public String toString() {
    return this.getClass().getSimpleName() + "{id: " + mId + "}";
  }

  public FlowTree getFlowTree() {
    if (mFlowTree == null) {
      initializeFlowTree();
    }
    return mFlowTree;
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

  @SuppressWarnings("ResultOfMethodCallIgnored") private void addEdgeToTree(Edge newEdge) {
    if (mEdges == null) {
      mEdges = new ArrayList<>();
    }
    Media parent = mMediaNodes.get(newEdge.getSourceIndex());
    Media child = mMediaNodes.get(newEdge.getTargetIndex());
    if (!getFlowTree().getNodes().containsKey(parent)) {
      getFlowTree().setRoot(parent);
    }
    getFlowTree().addNode(parent, child, newEdge.getReaction());
  }

  @SuppressWarnings("ResultOfMethodCallIgnored") private void initializeFlowTree() {
    mFlowTree = new FlowTree();
    if (!mMediaNodes.isEmpty()) {
      getFlowTree().setRoot(mMediaNodes.get(0));
    }
    if (mEdges != null) {
      for (Edge edge : mEdges) {
        addEdgeToTree(edge);
      }
    }
  }

  private String generatePartName(int mediaIndex) {
    return StudioApi.MEDIA_PART_PREFIX + mediaIndex;
  }
}
