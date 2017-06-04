package com.truethat.android.theater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.truethat.android.R;
import com.truethat.android.common.Emotion;
import com.truethat.android.common.Scene;
import com.truethat.android.common.util.Number;

import java.util.Locale;
import java.util.Map;

/**
 * Proudly created by ohad on 03/05/2017 for TrueThat.
 */

@SuppressLint("ViewConstructor")
public class SceneLayout extends ConstraintLayout {
    private Context mContext;

    public SceneLayout(Scene scene, Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ConstraintLayout layout = (ConstraintLayout) inflater
                .inflate(R.layout.fragment_scene, this);
        ImageView imageView = (ImageView) layout.findViewById(R.id.sceneImage);
        // Replaces the displayed image.
        if (scene.getImageBytes() != null) {
            final Bitmap bitmapImage = BitmapFactory
                    .decodeByteArray(scene.getImageBytes(), 0, scene.getImageBytes().length);
            imageView.setImageBitmap(bitmapImage);
        } else {
            Picasso.with(context)
                   .load(scene.getImageSignedUrl())
                   .placeholder(R.drawable.shower_dog)
                   .error(R.drawable.sad_dog)
                   .into(imageView);
        }

        // Sets the view count.
        TextView viewCountText = (TextView) layout.findViewById(R.id.viewCountText);
        viewCountText.setText(String.format(Locale.ENGLISH, "%d", scene.getViewCount()));

        if (scene.getReactionCounters() != null) {
            for (Map.Entry<Emotion, Long> emotionAndCounter : scene.getReactionCounters()
                                                                   .entrySet()) {
                addReactionCounterView(emotionAndCounter.getKey(), emotionAndCounter.getValue());
            }
        }
    }

    private void addReactionCounterView(Emotion emotion, long counter) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout layout = (LinearLayout) inflater
                .inflate(R.layout.fragment_reaction_counter, this);
        // Abbreviates the counter.
        TextView textView = (TextView) layout.findViewById(R.id.reactionCountText);
        textView.setText(Number.format(counter));
        // Sets the proper emotion emoji.
        ImageView imageView = (ImageView) layout.findViewById(R.id.emotionImage);
        imageView.setImageResource(Emotion.drawableResource(emotion));
    }
}
