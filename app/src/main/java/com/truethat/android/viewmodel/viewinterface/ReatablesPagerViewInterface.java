package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Reactable;
import java.util.List;
import retrofit2.Call;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface ReatablesPagerViewInterface extends BaseFragmentViewInterface {
  void displayItem(int index);

  Call<List<Reactable>> buildFetchReactablesCall();
}
