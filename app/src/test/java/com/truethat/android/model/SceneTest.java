package com.truethat.android.model;

import com.truethat.android.common.network.NetworkUtil;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 14/09/2017 for TrueThat.
 */
public class SceneTest {
  private static final Media PHOTO_0 = new Photo(0L, "0");
  private static final Media PHOTO_1 = new Photo(1L, "1");
  private static final Media PHOTO_2 = new Photo(2L, "2");
  private static final Media PHOTO_3 = new Photo(3L, "3");
  private static final Edge EDGE_0 = new Edge(0, 1, Emotion.HAPPY);
  private static final Edge EDGE_1 = new Edge(1, 2, Emotion.SURPRISE);
  private static final Edge EDGE_2 = new Edge(1, 3, Emotion.DISGUST);

  @Test public void createScene() throws Exception {
    Scene scene =
        new Scene(null, null, null, null, Arrays.asList(PHOTO_0, PHOTO_1, PHOTO_2, PHOTO_3),
            Arrays.asList(EDGE_0, EDGE_1, EDGE_2));
    assertEquals(PHOTO_0, scene.getRootMediaNode());
    assertEquals(PHOTO_1, scene.getNextMedia(PHOTO_0, Emotion.HAPPY));
    assertEquals(PHOTO_2, scene.getNextMedia(PHOTO_1, Emotion.SURPRISE));
    assertEquals(PHOTO_3, scene.getNextMedia(PHOTO_1, Emotion.DISGUST));
  }

  @Test public void createSceneFromJson() throws Exception {
    Scene scene = NetworkUtil.GSON.fromJson(NetworkUtil.GSON.toJson(
        new Scene(null, null, null, null, Arrays.asList(PHOTO_0, PHOTO_1, PHOTO_2, PHOTO_3),
            Arrays.asList(EDGE_0, EDGE_1, EDGE_2))), Scene.class);
    assertEquals(PHOTO_0, scene.getRootMediaNode());
    assertEquals(PHOTO_1, scene.getNextMedia(PHOTO_0, Emotion.HAPPY));
    assertEquals(PHOTO_2, scene.getNextMedia(PHOTO_1, Emotion.SURPRISE));
    assertEquals(PHOTO_3, scene.getNextMedia(PHOTO_1, Emotion.DISGUST));
  }
}