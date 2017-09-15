package com.truethat.android.model;

import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.viewmodel.ViewModelTestSuite;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 14/09/2017 for TrueThat.
 */
public class SceneTest extends ViewModelTestSuite {
  private static final Media PHOTO_0 = new Photo(0L, "0");
  private static final Media PHOTO_1 = new Photo(1L, "1");
  private static final Media PHOTO_2 = new Photo(2L, "2");
  private static final Media PHOTO_3 = new Photo(3L, "3");
  private static final Edge EDGE_0 = new Edge(0L, 1L, Emotion.HAPPY);
  private static final Edge EDGE_1 = new Edge(1L, 2L, Emotion.SURPRISE);
  private static final Edge EDGE_2 = new Edge(1L, 3L, Emotion.DISGUST);

  @Test public void createScene() throws Exception {
    Scene scene =
        new Scene(null, null, null, null, Arrays.asList(PHOTO_0, PHOTO_1, PHOTO_2, PHOTO_3),
            Arrays.asList(EDGE_0, EDGE_1, EDGE_2));
    assertEquals(PHOTO_0, scene.getRootMedia());
    assertEquals(PHOTO_1, scene.getNextMedia(PHOTO_0, Emotion.HAPPY));
    assertEquals(PHOTO_2, scene.getNextMedia(PHOTO_1, Emotion.SURPRISE));
    assertEquals(PHOTO_3, scene.getNextMedia(PHOTO_1, Emotion.DISGUST));
  }

  @Test public void createSceneFromJson() throws Exception {
    Scene scene = NetworkUtil.GSON.fromJson(NetworkUtil.GSON.toJson(
        new Scene(null, null, null, null, Arrays.asList(PHOTO_0, PHOTO_1, PHOTO_2, PHOTO_3),
            Arrays.asList(EDGE_0, EDGE_1, EDGE_2))), Scene.class);
    assertEquals(PHOTO_0, scene.getRootMedia());
    assertEquals(PHOTO_1, scene.getNextMedia(PHOTO_0, Emotion.HAPPY));
    assertEquals(PHOTO_2, scene.getNextMedia(PHOTO_1, Emotion.SURPRISE));
    assertEquals(PHOTO_3, scene.getNextMedia(PHOTO_1, Emotion.DISGUST));
  }

  @Test public void removeMedia() throws Exception {
    Scene scene =
        new Scene(null, null, null, null, Arrays.asList(PHOTO_0, PHOTO_1, PHOTO_2, PHOTO_3),
            Arrays.asList(EDGE_0, EDGE_1, EDGE_2));
    assertEquals(PHOTO_1, scene.removeMedia(PHOTO_3));
    assertEquals(3, scene.getMediaNodes().size());
    assertNull(scene.removeMedia(PHOTO_0));
    assertEquals(0, scene.getMediaNodes().size());
  }

  @Test public void addAndRemove() throws Exception {
    // Creates a scene from a photo
    Photo photo = new Photo(null, "1");
    Scene scene = new Scene(photo);
    // Photo should be allocated an ID.
    assertNotNull(photo.getId());
    Video video = new Video(null, "2");
    // Adds a new video to the scene.
    scene.addMedia(video, photo.getId(), Emotion.HAPPY);
    // Video should be allocated a unique ID.
    assertNotNull(video.getId());
    assertNotEquals(video.getId(), photo.getId());
    // Removes video from the scene
    assertEquals(photo, scene.removeMedia(video));
    // Adds a new video to the scene
    Video newVideo = new Video(null, "3");
    scene.addMedia(newVideo, photo.getId(), Emotion.HAPPY);
    // New video should be allocated a unique ID.
    assertNotEquals(video.getId(), newVideo.getId());
    // new video should follow photo.
    assertEquals(newVideo, scene.getNextMedia(photo, Emotion.HAPPY));
  }
}