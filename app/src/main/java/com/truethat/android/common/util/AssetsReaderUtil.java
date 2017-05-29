package com.truethat.android.common.util;

import android.content.Context;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */

public class AssetsReaderUtil {
    private static final int    BUFFER_SIZE  = 4 * 1024; // 4k
    private static final String CHARSET_NAME = "UTF-8";

    public static String read(Context context, String path) throws IOException {
        InputStream       inputStream = context.getAssets().open(path);
        StringBuilder     builder     = new StringBuilder();
        InputStreamReader reader      = new InputStreamReader(inputStream, CHARSET_NAME);
        char[]            buffer      = new char[BUFFER_SIZE];
        int               length;
        while ((length = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }

    public static byte[] readAsBytes(Context context, String path) throws IOException {
        InputStream inputStream = context.getAssets().open(path);
        return ByteStreams.toByteArray(inputStream);
    }
}