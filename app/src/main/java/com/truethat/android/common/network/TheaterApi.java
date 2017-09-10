package com.truethat.android.common.network;

import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/servlet/TheaterServlet.java</a>
 */

public interface TheaterApi {
  String PATH = "theater";

  /**
   * Get scenes from our backend to add some drama to our users life.
   */
  @POST(PATH) Call<List<Scene>> fetchScenes(@Body User user);
}
