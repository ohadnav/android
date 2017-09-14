package com.truethat.android.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Proudly created by ohad on 13/09/2017 for TrueThat.
 */
public class FlowTreeTest {
  private static final Photo PHOTO_1 = new Photo("1", null);
  private static final Photo PHOTO_2 = new Photo("2", null);

  @Test public void setRoot() {
    FlowTree tree = new FlowTree();
    tree.setRoot(PHOTO_1);
    assertEquals(PHOTO_1, tree.getRoot().getMedia());
    assertNotNull(tree.getNodes().get(PHOTO_1));
  }

  @Test public void addNode() {
    FlowTree tree = new FlowTree();
    tree.setRoot(PHOTO_1);
    tree.addNode(PHOTO_1, PHOTO_2, Emotion.FEAR);
    assertEquals(2, tree.getNodes().size());
    assertNotNull(tree.getNodes().get(PHOTO_2));
    assertEquals(PHOTO_2, tree.getNodes().get(PHOTO_1).getChildren().get(Emotion.FEAR).getMedia());
  }

  @Test public void removeNode() {
    FlowTree tree = new FlowTree();
    tree.setRoot(PHOTO_1);
    tree.addNode(PHOTO_1, PHOTO_2, Emotion.FEAR);
    tree.remove(PHOTO_2);
    assertEquals(1, tree.getNodes().size());
  }

  @Test public void removeParentNode() {
    FlowTree tree = new FlowTree();
    tree.setRoot(PHOTO_1);
    tree.addNode(PHOTO_1, PHOTO_2, Emotion.FEAR);
    tree.remove(PHOTO_1);
    assertEquals(0, tree.getNodes().size());
  }
}