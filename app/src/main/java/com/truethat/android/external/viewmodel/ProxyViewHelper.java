package com.truethat.android.external.viewmodel;

import android.support.annotation.NonNull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

/**
 * Proudly created by ohad on 24/07/2017 for TrueThat.
 */

public class ProxyViewHelper {

  private static final ProxyDummyClass sDummyClass = new ProxyDummyClass();
  private static final Class[] sInterfaces = new Class[1];
  private static final InvocationHandler sInvocationHandler = new InvocationHandler() {
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return null;
    }
  };

  @SuppressWarnings("unchecked") @NonNull public static <T> T init(@NonNull Class<?> in) {
    sInterfaces[0] = in;
    return (T) Proxy.newProxyInstance(sDummyClass.getClass().getClassLoader(), sInterfaces,
        sInvocationHandler);
  }

  /**
   * @param in           a generic type that should extend {@code whichExtends}.
   * @param whichExtends a class we expect the generic type of {@code in} to extend.
   *
   * @return the generic type of {@code in}'s type parameters that extends {@code whichExtends}, or
   * {@code whichExtends} if no such parameterized type exists.
   */
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
    return whichExtends;
  }

  private static final class ProxyDummyClass {
  }
}
