package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
import com.truethat.android.model.User;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/KfkLZp</a>
 */

public interface StudioAPI {
  String SCENE_IMAGE_PART = "image";
  String DIRECTOR_PART = "director";
  String CREATED_PART = "created";
  String USER_PARAM = "director_id";

  @Multipart @POST("studio") Call<ResponseBody> saveScene(@Part MultipartBody.Part image,
      @Part MultipartBody.Part director, @Part MultipartBody.Part created);

  @GET("studio") Call<List<Reactable>> getRepertoire(@Query(USER_PARAM) User user);
}
