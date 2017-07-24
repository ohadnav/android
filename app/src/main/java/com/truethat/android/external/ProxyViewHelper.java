package com.truethat.android.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Proudly created by ohad on 24/07/2017 for TrueThat.
 */

public class ProxyViewHelper {
  @Nullable
  public static Class<?> getGenericType(@NonNull Class<?> in, @NonNull Class<?> whichExtends) {
    final Type genericSuperclass = in.getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType) {
      final Type[] typeArgs = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
      for (Type arg : typeArgs) {
        if (arg instanceof ParameterizedType) {
          arg = ((ParameterizedType) arg).getRawType();
        }
        if (arg instanceof Class<?>) {
          final Class<?> argClass = (Class<?>) arg;
          if (whichExtends.isAssignableFrom(argClass)) {
            return argClass;
          }
        }
      }
    }
    return null;
  }
}
