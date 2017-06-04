package com.truethat.android.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 */

public class NetworkUtil {
    public static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();
}
