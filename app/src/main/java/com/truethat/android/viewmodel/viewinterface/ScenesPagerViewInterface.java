package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.ScenesPagerFragment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface ScenesPagerViewInterface extends BaseFragmentViewInterface {
  /**
   * Displays the {@code index}-th of {@link ScenesPagerFragment#mPager} item to user.
   *
   * @param index to display.
   */
  void displayItem(int index);

  /**
   * @return {@link Retrofit} api call to fetch scenes from our backend.
   */
  Call<List<Scene>> buildFetchScenesCall();
}
