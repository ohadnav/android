package com.truethat.android.common.network;

import com.truethat.android.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/servlet/AuthServlet.java</a>
 */

public interface AuthApi {
  String PATH = "auth";

  /**
   * Posts an authentication or an authorization to our magical backend.
   *
   * @param user current logged in user.
   *
   * @return the user, as stored in our backend.
   */
  @POST(PATH) Call<User> postAuth(@Body User user);
}
