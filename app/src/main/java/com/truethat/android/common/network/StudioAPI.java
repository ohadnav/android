package com.truethat.android.common.network;

import com.truethat.android.model.Reactable;
import com.truethat.android.model.User;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/KfkLZp</a>
 */

public interface StudioAPI {
  String PATH = "studio";
  String SCENE_IMAGE_PART = "scene-image";
  String REACTABLE_PART = "reactable";
  String USER_PARAM = "user";

  @Multipart @POST(PATH) Call<ResponseBody> saveReactable(@Part MultipartBody.Part reactable,
      @Part List<MultipartBody.Part> media);

  @GET(PATH) Call<List<Reactable>> getRepertoire(@Query(USER_PARAM) User user);
}
