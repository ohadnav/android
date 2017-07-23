package com.truethat.android.di.module;

import com.truethat.android.model.Reactable;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

@Module public class ReactableModule {
  private Reactable mReactable;

  public ReactableModule(Reactable reactable) {
    mReactable = reactable;
  }

  @Provides Reactable provideReactable() {
    return mReactable;
  }
}
