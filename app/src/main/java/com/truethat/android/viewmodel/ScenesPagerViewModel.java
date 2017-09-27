package com.truethat.android.viewmodel;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.AppContainer;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.viewinterface.ScenesPagerViewInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class ScenesPagerViewModel extends BaseFragmentViewModel<ScenesPagerViewInterface> {
  public final ObservableBoolean mNonFoundLayoutVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableList<Scene> mItems = new ObservableArrayList<>();
  private Callback<List<Scene>> mFetchScenesCallback;
  private Call<List<Scene>> mFetchScenesCall;
  private int mDisplayedIndex;
  private boolean mDetectReactions = false;

  public void setDetectReactions(boolean detectReactions) {
    mDetectReactions = detectReactions;
  }

  public void next() {
    // Fetch more scenes if we have none, or if we're at the last item.
    if (mItems.size() == 0 || mDisplayedIndex == mItems.size() - 1) {
      fetchScenes();
    } else {
      mDisplayedIndex = mDisplayedIndex + 1;
      getView().displayItem(mDisplayedIndex);
    }
  }

  public void previous() {
    if (mDisplayedIndex != 0) {
      mDisplayedIndex = mDisplayedIndex - 1;
      getView().displayItem(mDisplayedIndex);
    }
  }

  @Override public void onStop() {
    super.onStop();
    if (mFetchScenesCall != null) mFetchScenesCall.cancel();
    if (mDetectReactions) {
      AppContainer.getReactionDetectionManager().stop();
    }
  }

  @Override public void onStart() {
    super.onStart();
    mFetchScenesCallback = buildFetchScenesCallback();
  }

  /**
   * Fetching {@link Scene} from our backend.
   */
  public void fetchScenes() {
    Log.d(TAG, "Fetching scenes...");
    mNonFoundLayoutVisibility.set(false);
    if (mItems.isEmpty()) {
      mLoadingImageVisibility.set(true);
    }
    mFetchScenesCall = getView().buildFetchScenesCall();
    mFetchScenesCall.enqueue(mFetchScenesCallback);
  }

  @VisibleForTesting Scene getDisplayedScene() {
    if (mDisplayedIndex < 0 || mDisplayedIndex >= mItems.size()) {
      return null;
    }
    return mItems.get(mDisplayedIndex);
  }

  /**
   * @return fetching callback. If new {@link Scene}s are retrieved, then they are added to
   * {@link #mItems}.
   */
  private Callback<List<Scene>> buildFetchScenesCallback() {
    return new Callback<List<Scene>>() {
      @Override public void onResponse(@NonNull Call<List<Scene>> call,
          @NonNull Response<List<Scene>> response) {
        mLoadingImageVisibility.set(false);
        if (response.isSuccessful()) {
          List<Scene> fetchedScenes = response.body();
          if (fetchedScenes == null) {
            throw new AssertionError("I just cant believe it! The new scenes are null.");
          }
          List<Scene> newScenes = new ArrayList<>();
          for (Scene fetchedScene : fetchedScenes) {
            boolean isNew = true;
            for (Scene existingScene : mItems) {
              if (Objects.equals(existingScene.getId(), fetchedScene.getId())) {
                isNew = false;
              }
            }
            for (Scene alreadyConsidered : newScenes) {
              if (Objects.equals(alreadyConsidered.getId(), fetchedScene.getId())) {
                isNew = false;
              }
            }
            if (isNew) newScenes.add(fetchedScene);
          }
          if (newScenes.size() > 0) {
            // Find new scenes, to avoid duplicate scenes

            Log.v(TAG, "Loading " + newScenes.size() + " new scenes.");
            // Hides the loading image.
            // Display new scenes.
            int toDisplayIndex = mItems.size();
            mItems.addAll(newScenes);
            mDisplayedIndex = toDisplayIndex;
            getView().displayItem(mDisplayedIndex);
          } else if (mItems.size() == 0) {
            displayNotFound();
          }
        } else {
          if (!BuildConfig.DEBUG) {
            Crashlytics.logException(new Exception("Failed to fetch scenes"));
          }
          Log.e(TAG, "Failed to fetch scenes from "
              + call.request().url()
              + "\nUser: "
              + AppContainer.getAuthManager().getCurrentUser()
              + "\nResponse: "
              + response.code()
              + " "
              + response.message()
              + "\n"
              + response.headers());
          displayNotFound();
        }
      }

      @Override public void onFailure(@NonNull Call<List<Scene>> call, @NonNull Throwable t) {
        if (!BuildConfig.DEBUG) {
          Crashlytics.logException(t);
        }
        t.printStackTrace();
        Log.e(TAG, "Fetch scenes request to " + call.request().url() + " had failed.", t);
        displayNotFound();
      }
    };
  }

  private void displayNotFound() {
    // Shows not found text
    mNonFoundLayoutVisibility.set(true);
  }
}
