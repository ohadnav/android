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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import okhttp3.MultipartBody;
import retrofit2.Call;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * A media item that the user can have an emotional reaction to.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Scene.java</a>
 */
public class Scene extends BaseModel implements Serializable, FlowTree.Listener {
  private static final long serialVersionUID = -8890719190429524778L;
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
  /**
   * Allocates the next media ID.
   */
  private transient long mNextMediaId = 0;

  @VisibleForTesting
  public Scene(Long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      Media rootMedia) {
    super(id);
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
    mMediaNodes = new LinkedList<>(Collections.singletonList(rootMedia));
  }

  @VisibleForTesting
  public Scene(Long id, User director, TreeMap<Emotion, Long> reactionCounters, Date created,
      List<Media> mediaNodes, List<Edge> edges) {
    super(id);
    mDirector = director;
    mReactionCounters = reactionCounters;
    mCreated = created;
    mMediaNodes = new LinkedList<>(mediaNodes);
    mEdges = edges;
  }

  public Scene(Media media) {
    mDirector = AppContainer.getAuthManager().getCurrentUser();
    mCreated = new Date();
    mMediaNodes = new LinkedList<>();
    if (media.getId() == null) media.setId(mNextMediaId++);
    mMediaNodes.add(media);
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Scene() {
  }

  public List<Media> getMediaNodes() {
    return mMediaNodes;
  }

  /**
   * Adds {@code newMedia} to {@link #mFlowTree} and create and edge between it and the node of
   * {@code parentMediaId} colored with {@code reaction}.
   *
   * @param newMedia      what the user have just created.
   * @param parentMediaId the ID of the media that should lead to {@code newMedia} following a
   *                      {@code reaction}.
   * @param reaction      that should trigger a transition from the parent media to the new one.
   */
  public void addMedia(Media newMedia, long parentMediaId, Emotion reaction) {
    if (newMedia.getId() == null) newMedia.setId(mNextMediaId++);
    mMediaNodes.add(newMedia);
    Edge newEdge = new Edge(parentMediaId, newMedia.getId(), reaction);
    if (mEdges == null) {
      mEdges = new ArrayList<>();
    }
    mEdges.add(newEdge);
    // Add a new node and edge to the flow tree
    getFlowTree().addNode(newMedia);
    getFlowTree().addEdge(newEdge);
  }

  /**
   * @param current  what the user currently views, or have just created.
   * @param reaction how he reacted, or what he chose in the studio.
   *
   * @return the media that should follow {@code current} following a {@code reaction}, or null if
   * none such media exists.
   */
  @Nullable public Media getNextMedia(Media current, Emotion reaction) {
    return getFlowTree().getChild(current.getId(), reaction);
  }

  /**
   * @param current what the user currently views, or have just created.
   */
  @Nullable public Media getPreviousMedia(Media current) {
    return getFlowTree().getParent(current.getId());
  }

  /**
   * Removes the media and all its descendants from the flow tree.
   *
   * @param media to remove.
   *
   * @return the parent of the removed media.
   */
  @Nullable public Media removeMedia(Media media) {
    return getFlowTree().remove(media.getId());
  }

  public Media getRootMedia() {
    return getFlowTree().getRoot();
  }

  public Date getCreated() {
    return mCreated;
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

  /**
   * @return am API call for saving this scene.
   */
  @SuppressWarnings("unchecked") public Call<Scene> createApiCall() {
    if (!mFlowTree.isTree()) {
      throw new IllegalStateException("Flow tree is not a tree, I'm deeply disappointed!");
    }
    List<MultipartBody.Part> mediaParts = new ArrayList<>();
    for (Media media : mMediaNodes) {
      mediaParts.add(media.createPart());
    }
    MultipartBody.Part scenePart =
        MultipartBody.Part.createFormData(StudioApi.SCENE_PART, NetworkUtil.GSON.toJson(this));
    return NetworkUtil.createApi(StudioApi.class).saveScene(scenePart, mediaParts);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mDirector != null ? mDirector.hashCode() : 0);
    result = 31 * result + (mReactionCounters != null ? mReactionCounters.hashCode() : 0);
    result = 31 * result + (mCreated != null ? mCreated.hashCode() : 0);
    result = 31 * result + (mMediaNodes != null ? mMediaNodes.hashCode() : 0);
    result = 31 * result + (mEdges != null ? mEdges.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;
    if (!super.equals(o)) return false;

    Scene scene = (Scene) o;

    if (mDirector != null ? !mDirector.equals(scene.mDirector) : scene.mDirector != null) {
      return false;
    }
    if (mReactionCounters != null ? !mReactionCounters.equals(scene.mReactionCounters)
        : scene.mReactionCounters != null) {
      return false;
    }
    if (mCreated != null ? !mCreated.equals(scene.mCreated) : scene.mCreated != null) return false;
    if (mMediaNodes != null ? !mMediaNodes.equals(scene.mMediaNodes) : scene.mMediaNodes != null) {
      return false;
    }
    return mEdges != null ? mEdges.equals(scene.mEdges) : scene.mEdges == null;
  }

  @Override public String toString() {
    return this.getClass().getSimpleName() + "{id: " + mId + "}";
  }

  @Override public void deleteMedia(Media media) {
    mMediaNodes.remove(media);
  }

  @Override public void deleteEdge(Edge edge) {
    mEdges.remove(edge);
  }

  private FlowTree getFlowTree() {
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

  /**
   * Initializes the flow tree with existing media nodes and edges.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored") private void initializeFlowTree() {
    mFlowTree = new FlowTree(this);
    if (mMediaNodes != null) {
      for (Media media : mMediaNodes) {
        mFlowTree.addNode(media);
      }
    }
    if (mEdges != null) {
      for (Edge edge : mEdges) {
        mFlowTree.addEdge(edge);
      }
    }
    if (!mFlowTree.isTree()) {
      throw new IllegalStateException("Flow tree is not a tree, I'm deeply disappointed!");
    }
  }
}
