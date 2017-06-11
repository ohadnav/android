package com.truethat.android.theater;

import android.support.annotation.Nullable;
import com.truethat.android.common.Scene;
import com.truethat.android.common.network.EventCode;
import com.truethat.android.empathy.Emotion;
import java.util.Date;
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
  String USER_ID_FIELD = "user_id";
  String SCENE_ID_FIELD = "scene_id";
  String TIMESTAMP_FIELD = "timestamp";
  String EVENT_CODE_FIELD = "event_code";
  String EMOTION_FIELD = "emotion";

  /**
   * Get scenes from out beloved backend to add some drama to our users life.
   */
  @GET("/theater") Call<List<Scene>> getScenes();

  /**
   * Informs our backend of the the current user interaction with scenes.
   *
   * @param userId viewer's ID
   * @param sceneId of the displayed scene
   * @param timestamp of time of event
   * @param eventCode so that the event is recognized by our backend
   * @param reaction to the scene. Note that for reactable views this should be null.
   */
  @FormUrlEncoded @POST("/theater") Call<ResponseBody> postEvent(@Field(USER_ID_FIELD) Long userId,
      @Field(SCENE_ID_FIELD) Long sceneId, @Field(TIMESTAMP_FIELD) Date timestamp,
      @Field(EVENT_CODE_FIELD) EventCode eventCode, @Field(EMOTION_FIELD) @Nullable Emotion reaction);
}
