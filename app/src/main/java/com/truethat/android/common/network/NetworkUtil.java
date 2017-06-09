package com.truethat.android.common.network;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.android.BuildConfig;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 */

public class NetworkUtil {
  public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
  private static final String TAG = "NetworkUtil";
  private static String sBackendUrl = BuildConfig.BACKEND_URL;
  private static OkHttpClient CLIENT = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
    @Override public Response intercept(@NonNull Chain chain) throws IOException {
      Request request = chain.request();
      Log.v(TAG, "Sending " + request.method() + " request to " + request.url());
      Request newRequest =
          request.newBuilder().addHeader(HeadersContract.BUILD_NUMBER.getName(), BuildConfig.VERSION_NAME).build();
      return chain.proceed(newRequest);
    }
  }).build();

  public static <T> T createAPI(final Class<T> service) {
    Log.v(TAG, "Initializing API: " + service.getSimpleName());
    Retrofit retrofit = new Retrofit.Builder().baseUrl(getBackendUrl())
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .client(CLIENT)
        .build();

    return retrofit.create(service);
  }

  public static String getBackendUrl() {
    try {
      return sBackendUrl + ":" + BuildConfig.PORT;
    } catch (Exception ignored) {
      // PORT does not necessarily exists.
      return sBackendUrl;
    }
  }

  @VisibleForTesting public static void setBackendUrl(String backendUrl) {
    sBackendUrl = backendUrl;
  }
}
