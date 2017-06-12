package com.truethat.android.theater;

import com.truethat.android.common.Scene;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 */

interface TheaterAPI {
  String EVENT_FIELD = "event";

  /**
   * Get scenes from out beloved backend to add some drama to our users life.
   */
  @GET("/theater") Call<List<Scene>> getScenes();

  /**
   * Informs our backend of the the current user interaction with scenes.
   *
   * @param reactableEvent the encapsulates all the event information.
   */
  @FormUrlEncoded @POST("/theater") Call<ResponseBody> postEvent(
      @Field(EVENT_FIELD) ReactableEvent reactableEvent);
}
