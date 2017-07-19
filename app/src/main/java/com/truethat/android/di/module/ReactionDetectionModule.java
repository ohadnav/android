package com.truethat.android.di.module;

import com.truethat.android.di.scope.AppScope;
import com.truethat.android.empathy.DefaultReactionDetectionManager;
import com.truethat.android.empathy.EmotionDetectionClassifier;
import com.truethat.android.empathy.NullEmotionDetectionClassifier;
import com.truethat.android.empathy.ReactionDetectionManager;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

@Module public class ReactionDetectionModule {
  @Provides @AppScope EmotionDetectionClassifier provideEmotionDetectionClassifier() {
    return new NullEmotionDetectionClassifier();
  }

  @Provides @AppScope ReactionDetectionManager provideReactionDetectionManager(
      EmotionDetectionClassifier detectionClassifier) {
    return new DefaultReactionDetectionManager(detectionClassifier);
  }
}
