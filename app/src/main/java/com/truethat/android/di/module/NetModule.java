package com.truethat.android.di.module;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.android.BuildConfig;
import com.truethat.android.common.network.HeadersContract;
import com.truethat.android.di.scope.AppScope;
import com.truethat.android.external.GsonUTCDateAdapter;
import com.truethat.android.external.RuntimeTypeAdapterFactory;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import dagger.Module;
import dagger.Provides;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.regex.Pattern;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

@Module public class NetModule {
  /**
   * Pattern for field names with Android naming convention.
   */
  private static final Pattern ANDROID_NAMING_PATTERN = Pattern.compile("^m[A-Z].*");
  /**
   * Naming strategy to translate between Android field names and camel case names.
   */
  private static final FieldNamingStrategy NAMING_STRATEGY = new FieldNamingStrategy() {
    /**
     * @return removes the prefixed "m" from the field name.
     */
    @Override public String translateName(Field f) {
      String translatedName = f.getName();
      if (ANDROID_NAMING_PATTERN.matcher(f.getName()).matches()) {
        translatedName = Character.toLowerCase(f.getName().charAt(1)) + f.getName().substring(2);
      }
      return translatedName;
    }
  };

  private String mBaseUrl;

  public NetModule(String baseUrl) {
    mBaseUrl = baseUrl;
  }

  @Provides @AppScope public Gson provideGson() {
    GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingStrategy(NAMING_STRATEGY)
        .registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(Reactable.class).registerSubtype(Scene.class));
    return gsonBuilder.create();
  }

  @Provides @AppScope public OkHttpClient provideOkHttpClient() {
    return new OkHttpClient.Builder().addInterceptor(new Interceptor() {
      @Override public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Log.v(this.getClass().getSimpleName(),
            "Sending " + request.method() + " request to " + request.url());
        Request newRequest = request.newBuilder()
            .addHeader(HeadersContract.VERSION_NAME.getName(), BuildConfig.VERSION_NAME)
            .build();
        return chain.proceed(newRequest);
      }
    }).build();
  }

  @Provides @AppScope
  public Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
    return new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(mBaseUrl)
        .client(okHttpClient)
        .build();
  }
}
