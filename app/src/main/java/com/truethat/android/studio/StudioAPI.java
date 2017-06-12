package com.truethat.android.studio;

import com.truethat.android.common.Scene;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/KfkLZp</a>
 */

interface StudioAPI {
  String SCENE_IMAGE_PART = "image";
  String DIRECTOR_PART = "director_id";
  String CREATED_PART = "created";

  @Multipart @POST("/studio") Call<Scene> saveScene(@Part MultipartBody.Part image, @Part MultipartBody.Part directorId,
      @Part MultipartBody.Part created);
}
