package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
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
  String SCENE_IMAGE_PART = "scene_image";
  String REACTABLE_PART = "reactable";

  @Multipart @POST(PATH) Call<Reactable> saveReactable(@Part MultipartBody.Part reactable,
      @Part List<MultipartBody.Part> media);
}
