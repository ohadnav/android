package com.truethat.android.common.network;

import com.truethat.android.model.ReactableEvent;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 * <p>
 * * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/servlet/InteractionServlet.java</a>
 */

public interface InteractionApi {
  String PATH = "interaction";

  /**
   * Informs our backend of the the current user interaction with reactables.
   *
   * @param reactableEvent the encapsulates all the event information.
   */
  @POST(PATH) Call<ResponseBody> postEvent(@Body ReactableEvent reactableEvent);
}
