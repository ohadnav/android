package com.truethat.android.studio;

import com.truethat.android.BuildConfig;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 */

interface StudioAPI {
    String BASE_URL         = BuildConfig.BACKEND_URL + "studio/";
    String SCENE_IMAGE_PART = "scene_image";
    String CREATOR_PART     = "scene_creator_id";
    String TIMESTAMP_PART   = "scene_timestamp";

    @Multipart
    @POST("./")
    Call<Long> saveScene(@Part MultipartBody.Part image, @Part MultipartBody.Part creatorId,
                         @Part MultipartBody.Part timestamp);
}
