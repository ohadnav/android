package com.truethat.android.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import com.truethat.android.common.BaseApplicationTestSuite;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
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
    byte[] actual = CameraUtil.toByteArray(CameraTestUtil.bitmapBytesToMockedImage(expected, 0));
    assertArrayEquals(expected, actual);
  }

  @Test public void compareEquals() throws Exception {
    Image image = CameraTestUtil.bitmapBytesToMockedImage(
        AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(),
            CameraTestUtil.BITMAP_1x1_PATH), 0);
    assertTrue(CameraUtil.compare(image, image));
  }

  @Test public void compareNotEquals() throws Exception {
    byte[] source = AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(),
        CameraTestUtil.BITMAP_1x1_PATH);
    Bitmap bitmap =
        BitmapFactory.decodeByteArray(source, 0, source.length).copy(Bitmap.Config.ARGB_8888, true);
    bitmap.setPixel(0, 0, bitmap.getPixel(0, 0) + 100000);
    Image image1 = CameraTestUtil.bitmapBytesToMockedImage(source, 0);
    Image image2 =
        CameraTestUtil.bitmapBytesToMockedImage(CameraTestUtil.bitmapToByteArray(bitmap), 0);
    assertFalse(CameraUtil.compare(image1, image2));
  }
}