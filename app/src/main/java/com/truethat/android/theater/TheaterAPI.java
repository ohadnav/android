package com.truethat.android.theater;

import com.truethat.android.BuildConfig;
import com.truethat.android.common.Scene;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 */

interface TheaterAPI {
    String BASE_URL = BuildConfig.BACKEND_URL;

    @GET("/theater")
    Call<List<Scene>> getScenes();
}
