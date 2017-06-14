package com.truethat.android.theater;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.truethat.android.R;
import com.truethat.android.common.Scene;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.Emotion;

/**
 * Proudly created by ohad on 03/05/2017 for TrueThat.
 */

class SceneLayout {
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting static final Emotion DEFAULT_REACTION_COUNTER = Emotion.HAPPY;
  private Scene mScene;
  private ConstraintLayout mLayout;

  SceneLayout(Scene scene, ViewGroup rootView) {
    mLayout = new ConstraintLayout(rootView.getContext());
    mScene = scene;
    LayoutInflater inflater = LayoutInflater.from(rootView.getContext());
    mLayout = (ConstraintLayout) inflater.inflate(R.layout.fragment_scene, rootView, false);
    displayImage();
    updateReactionCounters(scene.getUserReaction());
    updateDirectorLayout();
  }

  ConstraintLayout getLayout() {
    return mLayout;
  }

  void doReaction(Emotion emotion) {
    // TODO(ohad): more visual.
    updateReactionCounters(emotion);
  }

  private void updateDirectorLayout() {
    // Sets the view count.
    TextView userNameText = (TextView) mLayout.findViewById(R.id.directorNameText);
    userNameText.setText(mScene.getDirector().getName());
    // Sets time ago
    TextView timeAgoText = (TextView) mLayout.findViewById(R.id.sceneTimeAgoText);
    timeAgoText.setText(DateUtil.formatTimeAgo(mScene.getCreated()));
  }

  private void displayImage() {
    ImageView imageView = (ImageView) mLayout.findViewById(R.id.sceneImage);

      Picasso.with(mLayout.getContext())
          .load(mScene.getImageSignedUrl())
          .placeholder(R.drawable.shower_dog)
          .error(R.drawable.sad_dog)
          .fit()
          .centerCrop()
          .into(imageView);
  }

  /**
   * Updates reactionCounterLayout with a proper counter and image.
   *
   * @param emotion if provided, then its drawable is displayed
   */
  private void updateReactionCounters(@Nullable Emotion emotion) {
    long sumCounts = 0;
    for (Long counter : mScene.getReactionCounters().values()) {
      sumCounts += counter;
    }
    // Abbreviates the counter.
    TextView reactionCountText = (TextView) mLayout.findViewById(R.id.reactionCountText);
    reactionCountText.setText(NumberUtil.format(sumCounts));
    // Sets the proper emotion emoji.
    Emotion toDisplay = DEFAULT_REACTION_COUNTER;
    if (emotion != null) {
      toDisplay = emotion;
    } else if (!mScene.getReactionCounters().isEmpty()) {
      toDisplay = mScene.getReactionCounters().lastKey();
    }
    ImageView imageView = (ImageView) mLayout.findViewById(R.id.reactionImage);
    imageView.setImageResource(toDisplay.getDrawableResource());
  }
}
