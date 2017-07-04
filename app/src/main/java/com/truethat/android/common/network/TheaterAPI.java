package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
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

public interface TheaterAPI {
  String PATH = "theater";

  /**
   * Get reactables from our backend to append some drama to our users life.
   */
  @POST(PATH) Call<List<Reactable>> fetchReactables(@Body User user);
}
