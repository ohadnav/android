package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
import com.truethat.android.model.ReactableEvent;
import com.truethat.android.model.User;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/PbBPFT</a>
 */

public interface TheaterAPI {
  String PATH = "theater";
  String USER_PARAM = "user";

  /**
   * Get reactables from out beloved backend to add some drama to our users life.
   */
  @GET(PATH) Call<List<Reactable>> getReactables(@Query(USER_PARAM) User user);

  /**
   * Informs our backend of the the current user interaction with reactables.
   *
   * @param reactableEvent the encapsulates all the event information.
   */
  @POST(PATH) Call<ResponseBody> postEvent(@Body ReactableEvent reactableEvent);
}
