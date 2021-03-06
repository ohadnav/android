package com.truethat.android.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 13/09/2017 for TrueThat.
 */
public class FlowTreeTest {
  private static final Photo PHOTO_1 = new Photo(1L, "1");
  private static final Photo PHOTO_2 = new Photo(2L, "2");
  private static final Photo PHOTO_3 = new Photo(3L, "3");
  private static final Edge EDGE_1 = new Edge(1L, 2L, Emotion.OMG);
  private static final Edge EDGE_2 = new Edge(2L, 3L, Emotion.HAPPY);
  private FlowTree mTree;
  private FakeFlowTreeListener mFlowTreeListener;

  @Before public void setUp() throws Exception {
    mFlowTreeListener = new FakeFlowTreeListener();
    mTree = new FlowTree(mFlowTreeListener);
  }

  @Test public void getRoot() {
    assertNull(mTree.getRoot());
    mTree.addNode(PHOTO_1);
    assertEquals(PHOTO_1, mTree.getRoot());
    mTree.addNode(PHOTO_2);
    mTree.addEdge(EDGE_1);
    assertEquals(PHOTO_1, mTree.getRoot());
  }

  @Test public void addNode() {
    mTree.addNode(PHOTO_1, PHOTO_2);
    assertEquals(2, mTree.getNodes().size());
    assertEquals(PHOTO_1, mTree.getMedia(PHOTO_1.getId()));
    assertEquals(PHOTO_2, mTree.getMedia(PHOTO_2.getId()));
  }

  @Test public void getChild() {
    mTree.addNode(PHOTO_1, PHOTO_2);
    mTree.addEdge(EDGE_1);
    assertEquals(PHOTO_2, mTree.getChild(PHOTO_1.getId(), EDGE_1.getReaction()));
  }

  @Test public void removeNode() {
    mTree.addNode(PHOTO_1, PHOTO_2, PHOTO_3);
    mTree.addEdge(EDGE_1, EDGE_2);
    mTree.remove(PHOTO_2.getId());
    assertEquals(1, mTree.getNodes().size());
    assertTrue(mFlowTreeListener.mDeletedMedia.contains(PHOTO_2));
    assertTrue(mFlowTreeListener.mDeletedMedia.contains(PHOTO_3));
    assertTrue(mFlowTreeListener.mDeletedEdges.contains(EDGE_1));
    assertTrue(mFlowTreeListener.mDeletedEdges.contains(EDGE_2));
  }

  @Test public void removeRootNode() {
    mTree.addNode(PHOTO_1, PHOTO_2, PHOTO_3);
    mTree.addEdge(EDGE_1, EDGE_2);
    mTree.remove(PHOTO_1.getId());
    assertEquals(0, mTree.getNodes().size());
    assertNull(mTree.getRoot());
    assertTrue(mFlowTreeListener.mDeletedMedia.contains(PHOTO_1));
    assertTrue(mFlowTreeListener.mDeletedMedia.contains(PHOTO_2));
  }

  @Test public void isTree() {
    assertTrue(mTree.isTree());
    mTree.addNode(PHOTO_1);
    assertTrue(mTree.isTree());
    mTree.addNode(PHOTO_2);
    assertFalse(mTree.isTree());
    mTree.addEdge(EDGE_1);
    assertTrue(mTree.isTree());
  }

  @Test public void getMediaReturnsNull() {
    assertNull(mTree.getMedia(PHOTO_2.getId()));
    mTree.addNode(PHOTO_1);
    assertNull(mTree.getMedia(PHOTO_2.getId()));
  }

  @Test(expected = IllegalArgumentException.class) public void getChildFails_noChildNode()
      throws Exception {
    mTree.addNode(PHOTO_1, PHOTO_2);
    mTree.addEdge(EDGE_1);
    mTree.getChild(PHOTO_1.getId() + PHOTO_2.getId(), Emotion.OMG);
  }

  @Test(expected = IllegalArgumentException.class) public void getParentFails() {
    mTree.addNode(PHOTO_1);
    mTree.getParent(PHOTO_2.getId());
  }

  @Test(expected = IllegalArgumentException.class) public void addEdgeFails_noChildNode()
      throws Exception {
    mTree.addNode(PHOTO_1, PHOTO_2);
    mTree.addEdge(new Edge(PHOTO_1.getId(), -PHOTO_2.getId(), Emotion.HAPPY));
  }

  @Test(expected = IllegalArgumentException.class) public void addEdgeFails_noParentNode()
      throws Exception {
    mTree.addNode(PHOTO_1, PHOTO_2);
    mTree.addEdge(new Edge(-PHOTO_1.getId(), PHOTO_2.getId(), Emotion.HAPPY));
  }

  private class FakeFlowTreeListener implements FlowTree.Listener {
    private List<Media> mDeletedMedia = new ArrayList<>();
    private List<Edge> mDeletedEdges = new ArrayList<>();

    @Override public void deleteMedia(Media media) {
      mDeletedMedia.add(media);
    }

    @Override public void deleteEdge(Edge edge) {
      mDeletedEdges.add(edge);
    }
  }
}