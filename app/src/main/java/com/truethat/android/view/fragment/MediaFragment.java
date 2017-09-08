package com.truethat.android.view.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentMediaBinding;
import com.truethat.android.model.Media;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Pose;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public abstract class MediaFragment<Model extends Media> extends
    BaseFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentMediaBinding> {
  private static final String ARG_MEDIA = "media";
  Model mMedia;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  MediaListener mMediaListener;
  boolean mIsReady = false;

  /**
   * Prepares {@code fragment} for creation in implementing class, such as {@link
   * PhotoFragment#newInstance(Photo)}.
   *
   * @param fragment to prepare.
   * @param media    to associate with this fragment.
   */
  static void prepareInstance(Fragment fragment, Media media) {
    Bundle args = new Bundle();
    args.putSerializable(ARG_MEDIA, media);
    fragment.setArguments(args);
  }

  public void setMediaListener(MediaListener mediaListener) {
    mMediaListener = mediaListener;
  }

  public boolean isReady() {
    return mIsReady;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mMedia = (Model) getArguments().getSerializable(ARG_MEDIA);
  }

  /**
   * Creation of media layout, such as a Pose image, is done by implementations.
   */
  @SuppressWarnings("unchecked") @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    inflater.inflate(getLayoutResource(), (ViewGroup) mRootView.findViewById(R.id.mediaLayout));
    // Binds views with butterknife.
    mViewUnbinder = ButterKnife.bind(this, mRootView);
    return mRootView;
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onDetach() {
    super.onDetach();
    mMediaListener = null;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_media, getContext());
  }

  /**
   * Create the media layout of the fragment, such as the {@link Pose} image.
   */
  abstract @LayoutRes int getLayoutResource();

  public interface MediaListener {
    /**
     * Called once the media has been downloaded.
     */
    void onReady();
  }
}
