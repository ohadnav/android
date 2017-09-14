package com.truethat.android.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import com.truethat.android.common.BaseApplicationTestSuite;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 28/05/2017 for TrueThat.
 */
public class CameraUtilTest extends BaseApplicationTestSuite {

  @Test public void toByteArray() throws Exception {
    // Asserts that an exception is not thrown, as the actual image cannot be expected.
    byte[] expected = AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(),
        CameraTestUtil.BITMAP_1x1_PATH);
    byte[] actual = CameraUtil.toByteArray(CameraTestUtil.bitmapBytesToMockedImage(expected));
    assertArrayEquals(expected, actual);
  }

  @Test public void compareEquals() throws Exception {
    Image image = CameraTestUtil.bitmapBytesToMockedImage(
        AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(),
            CameraTestUtil.BITMAP_1x1_PATH));
    assertTrue(CameraUtil.compare(image, image));
  }

  @Test public void compareNotEquals() throws Exception {
    byte[] source = AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(),
        CameraTestUtil.BITMAP_1x1_PATH);
    Bitmap bitmap =
        BitmapFactory.decodeByteArray(source, 0, source.length).copy(Bitmap.Config.ARGB_8888, true);
    bitmap.setPixel(0, 0, bitmap.getPixel(0, 0) + 100000);
    Image image1 = CameraTestUtil.bitmapBytesToMockedImage(source);
    Image image2 =
        CameraTestUtil.bitmapBytesToMockedImage(CameraTestUtil.bitmapToByteArray(bitmap));
    assertFalse(CameraUtil.compare(image1, image2));
  }

  @Test public void scaleFit() throws Exception {
    final Point displaySize = AppUtil.realDisplaySize(mActivityTestRule.getActivity());
    assertEquals(displaySize, CameraUtil.scaleFit(displaySize, displaySize));
    assertEquals(displaySize,
        CameraUtil.scaleFit(new Point(displaySize.x / 2, displaySize.y / 2), displaySize));
    assertEquals(displaySize,
        CameraUtil.scaleFit(new Point(displaySize.x * 2, displaySize.y * 2), displaySize));
    assertEquals(new Point(displaySize.x, displaySize.y * 2),
        CameraUtil.scaleFit(new Point(displaySize.x / 2, displaySize.y), displaySize));
    assertEquals(new Point(displaySize.x * 2, displaySize.y),
        CameraUtil.scaleFit(new Point(displaySize.x, displaySize.y / 2), displaySize));
    assertEquals(new Point(displaySize.x, displaySize.y * 3 / 2),
        CameraUtil.scaleFit(new Point(displaySize.x / 3, displaySize.y / 2), displaySize));
  }
}