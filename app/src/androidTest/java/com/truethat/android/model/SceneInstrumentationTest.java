package com.truethat.android.model;

import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;
import org.junit.Test;

import static com.truethat.android.common.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */
public class SceneInstrumentationTest {

  @Test public void testParcelable() throws Exception {
    TreeMap<Emotion, Long> treeMap = new TreeMap<>();
    treeMap.put(Emotion.DISGUST, 10L);
    testParcelability(new Scene(1L, new User(1L, "a", "b", "c"), treeMap, new Date(),
            Arrays.asList(new Video(1L, "a"), new Photo(2L, "b"), new Video(3L, "c")),
            Arrays.asList(new Edge(1L, 2L, Emotion.DISGUST), new Edge(1L, 3L, Emotion.OMG))),
        Scene.CREATOR);
    // null reaction counters
    testParcelability(new Scene(1L, new User(1L, "a", "b", "c"), null, new Date(),
            Arrays.asList(new Video(1L, "a"), new Photo(2L, "b"), new Video(3L, "c")),
            Arrays.asList(new Edge(1L, 2L, Emotion.DISGUST), new Edge(1L, 3L, Emotion.OMG))),
        Scene.CREATOR);
  }
}