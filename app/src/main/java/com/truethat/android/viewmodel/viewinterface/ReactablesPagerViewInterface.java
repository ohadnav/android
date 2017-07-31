package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.ReactablesPagerFragment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface ReactablesPagerViewInterface extends BaseFragmentViewInterface {
  /**
   * Displays the {@code index}-th of {@link ReactablesPagerFragment#mPager} item to user.
   *
   * @param index to display.
   */
  void displayItem(int index);

  /**
   * @return {@link Retrofit} api call to fetch reactables from our backend.
   */
  Call<List<Reactable>> buildFetchReactablesCall();
}
