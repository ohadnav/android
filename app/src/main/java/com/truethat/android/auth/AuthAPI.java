package com.truethat.android.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

interface AuthAPI {
  /**
   * Posts an authentication or an authorization to our magical backend.
   *
   * @param user current logged in user.
   * @return the user, as stored in our backend.
   */
  @POST("auth") Call<User> postAuth(@Body User user);
}
