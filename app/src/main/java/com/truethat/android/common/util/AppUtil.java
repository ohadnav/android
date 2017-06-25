package com.truethat.android.common.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

/**
 * Proudly created by ohad on 25/06/2017 for TrueThat.
 */

public class AppUtil {
  public static Size availableDisplaySize(View view) {
    Rect windowRect = new Rect();
    view.getWindowVisibleDisplayFrame(windowRect);
    Size availableSize =
        new Size(windowRect.bottom - windowRect.top, windowRect.right - windowRect.left);
    // Sizes are naturally inverted, and so inverse size if we are in a portrait orientation.
    Display display = ((WindowManager) view.getContext()
        .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    if (display.getRotation() == Surface.ROTATION_0
        || display.getRotation() == Surface.ROTATION_180) {
      availableSize = new Size(availableSize.getHeight(), availableSize.getWidth());
    }
    return availableSize;
  }

  public static Point realDisplaySize(Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point size = new Point();
    display.getRealSize(size);
    return size;
  }

  public static boolean isEmulator() {
    return Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk".equals(Build.PRODUCT);
  }
}
