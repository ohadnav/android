package com.truethat.android.common.util;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Proudly created by ohad on 27/06/2017 for TrueThat.
 */

public class CountingDispatcher extends Dispatcher {
  private Map<String, Integer> mPathToCount = new HashMap<>();

  public int getCount(String path) {
    if (!mPathToCount.containsKey(path)) {
      return 0;
    }
    return mPathToCount.get(path);
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
    String path = request.getPath().substring(1);
    if (!mPathToCount.containsKey(path)) {
      mPathToCount.put(path, 0);
    }
    mPathToCount.put(path, mPathToCount.get(path) + 1);
  }
}
