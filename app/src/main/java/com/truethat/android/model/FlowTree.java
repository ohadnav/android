package com.truethat.android.model;

import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 13/09/2017 for TrueThat.
 */

public class FlowTree {
  private Node mRoot;
  private Map<Media, Node> mNodes = new HashMap<>();

  public Node getRoot() {
    return mRoot;
  }

  public synchronized void setRoot(Media media) {
    mRoot = new Node(media, null);
    mNodes.put(media, mRoot);
  }

  public synchronized void remove(Media media) {
    if (mNodes.containsKey(media)) {
      Node node = mNodes.get(media);
      if (node.getParent() != null) {
        node.getParent().removeChild(node);
      }
      for (Node childNode : node.mChildren.values()) {
        remove(childNode.getMedia());
      }
      mNodes.remove(media);
    }
    if (mNodes.isEmpty()) {
      mRoot = null;
    }
  }

  public Map<Media, Node> getNodes() {
    return mNodes;
  }

  synchronized void addNode(Media parent, Media child, Emotion emotion) {
    if (!mNodes.containsKey(parent)) {
      throw new IllegalStateException("Parent node must already be added to the tree.");
    }
    remove(child);
    Node parentNode = mNodes.get(parent);
    Node childNode = parentNode.addChild(child, emotion);
    mNodes.put(child, childNode);
  }

  @SuppressWarnings("WeakerAccess") public static class Node {
    private Media mMedia;
    private Node mParent;
    private Map<Emotion, Node> mChildren = new HashMap<>();

    Node(Media media, @Nullable Node parent) {
      mMedia = media;
      mParent = parent;
    }

    public Media getMedia() {
      return mMedia;
    }

    public Node getParent() {
      return mParent;
    }

    @Override public int hashCode() {
      return mMedia != null ? mMedia.hashCode() : 0;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Node)) return false;

      Node node = (Node) o;

      return mMedia != null ? mMedia.equals(node.mMedia) : node.mMedia == null;
    }

    @Override public String toString() {
      return "Node: (media = " + mMedia + "\nparent = " + mParent + "\nchildren = " + mChildren;
    }

    Map<Emotion, Node> getChildren() {
      return mChildren;
    }

    synchronized Node addChild(Media media, Emotion emotion) {
      Node childNode = new Node(media, this);
      mChildren.put(emotion, childNode);
      return childNode;
    }

    synchronized void removeChild(Node node) {
      for (Map.Entry<Emotion, Node> childEntry : mChildren.entrySet()) {
        if (node.equals(childEntry.getValue())) {
          mChildren.remove(childEntry.getKey());
          return;
        }
      }
    }
  }
}
