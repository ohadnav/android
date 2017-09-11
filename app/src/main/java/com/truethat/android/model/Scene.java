package com.truethat.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
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
   * The user reaction to the scene, {@code null} for no reaction.
   */
  private Emotion mUserReaction;
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
   * Whether the scene was viewed by the user.
   */
  private boolean mViewed;
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
  private transient MutableValueGraph<Media, Emotion> mMediaGraph;

  @VisibleForTesting
  public Scene(long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      @Nullable Emotion userReaction, Media rootMedia) {
    mId = id;
    mUserReaction = userReaction;
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
    mMediaNodes = Collections.singletonList(rootMedia);
    //if (edges != null) {
    //  mEdges = edges;
    //  for (Edge edge : mEdges) {
    //    updateGraph(edge);
    //  }
    //}
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

  public List<Edge> getEdges() {
    return mEdges;
  }

  public MutableValueGraph<Media, Emotion> getMediaGraph() {
    if (mMediaGraph == null) {
      initializeMediaGraph();
    }
    return mMediaGraph;
  }

  public void addMedia(Media source, Media target, Emotion reaction) {
    mMediaNodes.add(target);
    Edge newEdge =
        new Edge((long) mMediaNodes.indexOf(source), (long) mMediaNodes.indexOf(target), reaction);
    mEdges.add(newEdge);
    updateGraph(newEdge);
  }

  @Nullable public Media getNextMedia(Media current, Emotion reaction) {
    for (Media optionalNext : mMediaGraph.adjacentNodes(current)) {
      if (mMediaGraph.edgeValue(current, optionalNext) == reaction) {
        return optionalNext;
      }
    }
    return null;
  }

  public Media getRootMediaNode() {
    return mMediaNodes.get(0);
  }

  public Date getCreated() {
    return mCreated;
  }

  public Long getId() {
    return mId;
  }

  @VisibleForTesting public void setId(Long id) {
    mId = id;
  }

  public boolean isViewed() {
    return mViewed;
  }

  /**
   * Marks this Scene as viewed by the user.
   */
  public void doView() {
    mViewed = true;
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
    // Check if the user had already reacted this scene.
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

  @SuppressWarnings("unchecked") public Call<Scene> createApiCall() {
    List<MultipartBody.Part> mediaParts = new ArrayList<>();
    for (int i = 0; i < mMediaNodes.size(); i++) {
      mediaParts.add(mMediaNodes.get(i).createPart(generatePartName(i)));
    }
    MultipartBody.Part scenePart =
        MultipartBody.Part.createFormData(StudioApi.SCENE_PART, NetworkUtil.GSON.toJson(this));
    return NetworkUtil.createApi(StudioApi.class).saveScene(scenePart, mediaParts);
  }

  /**
   * @param user that should react to this scene.
   *
   * @return whether {@code user} can react to this scene.
   */
  public boolean canReactTo(User user) {
    boolean notReactedTo = mUserReaction == null;
    boolean notMine = !user.equals(mDirector);
    return notReactedTo && notMine;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;

    Scene scene = (Scene) o;

    if (!Objects.equals(mId, scene.mId)) return false;
    if (mViewed != scene.mViewed) return false;
    if (mUserReaction != scene.mUserReaction) return false;
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

  @SuppressWarnings("ResultOfMethodCallIgnored") private void updateGraph(Edge newEdge) {
    if (mMediaGraph == null) {
      initializeMediaGraph();
    }
    Media sourceNode = mMediaNodes.get(newEdge.getSourceIndex().intValue());
    Media targetNode = mMediaNodes.get(newEdge.getTargetIndex().intValue());
    if (!mMediaGraph.nodes().contains(sourceNode)) {
      mMediaGraph.addNode(sourceNode);
    }
    if (!mMediaGraph.nodes().contains(targetNode)) {
      mMediaGraph.addNode(targetNode);
    }
    mMediaGraph.putEdgeValue(sourceNode, targetNode, newEdge.getReaction());
  }

  @SuppressWarnings("ResultOfMethodCallIgnored") private void initializeMediaGraph() {
    mMediaGraph = ValueGraphBuilder.directed().expectedNodeCount(mMediaNodes.size()).build();
    for (Media mediaNode : mMediaNodes) {
      mMediaGraph.addNode(mediaNode);
    }
    for (Edge edge : mEdges) {
      updateGraph(edge);
    }
  }

  private String generatePartName(int mediaIndex) {
    return StudioApi.MEDIA_PART_PREFIX + mediaIndex;
  }
}
