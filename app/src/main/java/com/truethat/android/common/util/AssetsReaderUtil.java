package com.truethat.android.common.util;

import android.content.Context;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */

class AssetsReaderUtil {
  /**
   * @param context of which the asset is belonged to.
   * @param path    relative path to asset from within the context's asserts directory.
   * @return the app asset as byte array.
   */
  static byte[] readAsBytes(Context context, String path) throws IOException {
    InputStream inputStream = context.getAssets().open(path);
    return ByteStreams.toByteArray(inputStream);
  }
}