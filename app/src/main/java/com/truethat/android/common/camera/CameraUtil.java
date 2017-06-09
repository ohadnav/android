package com.truethat.android.common.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.support.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class CameraUtil {
  @Nullable public static String getFrontFacingCameraId(CameraManager manager) {
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        Integer orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (orientation == null) continue;
        if (orientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @return byte array that represents the first plane of the given Image.
   */
  public static byte[] toByteArray(Image image) {
    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
    buffer.rewind();
    final byte[] pixels = new byte[buffer.limit()];
    buffer.get(pixels);
    return pixels;
  }

  public static boolean compare(Image image1, Image image2) {
    return image1.getFormat() == image2.getFormat()
        && image1.getHeight() == image2.getHeight()
        && image1.getWidth() == image2.getWidth()
        && Arrays.equals(toByteArray(image1), toByteArray(image2));
  }
}
