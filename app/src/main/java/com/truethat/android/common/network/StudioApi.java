package com.truethat.android.common.network;

import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/KfkLZp</a>
 */

public interface StudioApi {
  String PATH = "studio";
  /**
   * HTTP part name of a {@link Pose}'s image.
   */
  String pose_IMAGE_PART = "pose_image";
  /**
   * HTTP part name of the {@link Reactable} data.
   */
  String REACTABLE_PART = "reactable";

  /**
   * Saves a {@link Reactable} in out magical backend.
   *
   * @param reactable to save
   * @param media     files to save in storage, such as a {@link Pose}'s image.
   *
   * @return {@link Retrofit} call.
   */
  @Multipart @POST(PATH) Call<Reactable> saveReactable(@Part MultipartBody.Part reactable,
      @Part List<MultipartBody.Part> media);
}
