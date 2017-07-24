package com.truethat.android.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Proudly created by ohad on 24/07/2017 for TrueThat.
 */

public class ProxyViewHelper {
  /**
   * @param in           a generic type that should extend {@code whichExtends}.
   * @param whichExtends a class we expect the generic type of {@code in} to extend.
   *
   * @return the generic type of {@code in} that extends {@code whichExtends}.
   */
  @Nullable
  public static Class<?> getGenericType(@NonNull Class<?> in, @NonNull Class<?> whichExtends) {
    final Type genericSuperclass = in.getGenericSuperclass();
    // If it has no parameters, then exit.
    if (genericSuperclass instanceof ParameterizedType) {
      // Get superclasses types of `in`.
      final Type[] typeArgs = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
      for (Type arg : typeArgs) {
        // If the generic type is again generic, then simplify it.
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
