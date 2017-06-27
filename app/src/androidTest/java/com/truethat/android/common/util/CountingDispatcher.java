package com.truethat.android.common.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.net.HttpURLConnection;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Proudly created by ohad on 27/06/2017 for TrueThat.
 */

public class CountingDispatcher extends Dispatcher {
  private Table<String, String, Integer> mCounters = HashBasedTable.create();

  public int getCount(String method, String subPath) {
    if (!mCounters.contains(method, subPath)) {
      return 0;
    }
    return mCounters.get(method, subPath);
  }

  @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
    count(request);
    MockResponse response =
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    try {
      response = processRequest(request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return response;
  }

  @SuppressWarnings({ "UnusedParameters", "WeakerAccess" })
  public MockResponse processRequest(RecordedRequest request) throws Exception {
    return new MockResponse();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored") private void count(RecordedRequest request) {
    // Removes initial "/"
    String subPath = request.getPath().substring(1);
    if (!mCounters.contains(request.getMethod(), subPath)) {
      mCounters.put(request.getMethod(), subPath, 0);
    }
    mCounters.put(request.getMethod(), subPath, mCounters.get(request.getMethod(), subPath) + 1);
  }
}
