package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
import com.truethat.android.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public interface RepertoireAPI {
  String PATH = "repertoire";

  /**
   * Retrieves a list of the {@link Reactable}s created by {@code user}.
   *
   * @param user for which to pull the repertoire.
   */
  @POST(PATH) Call<List<Reactable>> fetchRepertoire(@Body User user);
}
