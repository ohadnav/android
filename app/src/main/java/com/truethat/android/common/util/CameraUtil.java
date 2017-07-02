package com.truethat.android.common.util;

import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class CameraUtil {
  /**
   * Compares two {@code Size}s based on their areas.
   */
  public static final Comparator<Size> SIZE_AREA_COMPARATOR = new Comparator<Size>() {
    @Override public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  };
  private static final String TAG = "CameraUtil";

  @Nullable public static String getCameraId(CameraManager manager, Facing facing) {
    String result = null;
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        Integer orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (orientation == null) continue;
        if (facing == Facing.FRONT && orientation == CameraCharacteristics.LENS_FACING_FRONT) {
          result = cameraId;
        } else if (facing == Facing.BACK && orientation == CameraCharacteristics.LENS_FACING_BACK) {
          result = cameraId;
        }
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
    return result;
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

  /**
   * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
   * is at least as large as the respective texture view size, and that is at most as large as the
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size,
   * and whose aspect ratio matches with the specified value.
   *
   * @param choices           The list of sizes that the camera supports for the intended output
   *                          class
   * @param textureViewWidth  The width of the texture view relative to sensor coordinate
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   * @param maxWidth          The maximum width that can be chosen
   * @param maxHeight         The maximum height that can be chosen
   * @param aspectRatio       The aspect ratio
   *
   * @return The optimal {@code Size}, or the largest one.
   */
  public static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight,
      int maxWidth, int maxHeight, Point aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.x;
    int h = aspectRatio.y;
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
          && option.getHeight() <= maxHeight
          && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, SIZE_AREA_COMPARATOR);
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, SIZE_AREA_COMPARATOR);
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return Collections.max(Arrays.asList(choices), SIZE_AREA_COMPARATOR);
    }
  }

  /**
   * Where the camera is facing.
   */
  public enum Facing {
    /**
     * Selfie camera - front facing camera.
     */
    FRONT, /**
     * Back camera, usually the one with higher quality.
     */
    BACK
  }
}
