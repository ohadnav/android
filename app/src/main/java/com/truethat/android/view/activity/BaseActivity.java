package com.truethat.android.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.empathy.ReactionDetectionManager;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;
import eu.inloop.viewmodel.ProxyViewHelper;
import eu.inloop.viewmodel.ViewModelHelper;
import eu.inloop.viewmodel.base.ViewModelBaseEmptyActivity;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import javax.inject.Inject;
import retrofit2.Retrofit;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity<ViewInterface extends BaseViewInterface, ViewModelType extends BaseViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends ViewModelBaseEmptyActivity implements BaseViewInterface {
  @NonNull private final ViewModelHelper<ViewInterface, ViewModelType> mViewModeHelper =
      new ViewModelHelper<>();
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  protected String TAG = this.getClass().getSimpleName();
  /**
   * Whether to skip authentication.
   */
  protected boolean mSkipAuth = false;
  @BindView(R.id.activityRootView) protected View mRootView;

  @Inject Retrofit mRetrofit;

  @Inject Gson mGson;

  @Inject PermissionsManager mPermissionsManager;

  @Inject AuthManager mAuthManager;

  @Inject DeviceManager mDeviceManager;

  @Inject ReactionDetectionManager mReactionDetectionManager;

  /**
   * Permission not granted callback.
   *
   * @param permission the rejected permission.
   */
  @MainThread public void onRequestPermissionsFailed(Permission permission) {
    Intent askForPermission = new Intent(this, AskForPermissionActivity.class);
    askForPermission.putExtra(AskForPermissionActivity.EXTRA_PERMISSION, permission);
    startActivityForResult(askForPermission, permission.getRequestCode());
  }

  /**
   * Authentication success callback.
   */
  public void onAuthOk() {
    Log.v(TAG, "onAuthOk");
  }

  /**
   * Authentication failure callback.
   */
  public void onAuthFailed() {
    Log.v(TAG, "Auth failed. Something smells bad...");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        Intent authFailed = new Intent(BaseActivity.this, WelcomeActivity.class);
        authFailed.putExtra(WelcomeActivity.EXTRA_AUTH_FAILED, true);
        startActivity(authFailed);
      }
    });
  }

  public <T> T createApiInterface(final Class<T> service) {
    return mRetrofit.create(service);
  }

  public ReactionDetectionManager getReactionDetectionManager() {
    return mReactionDetectionManager;
  }

  public PermissionsManager getPermissionsManager() {
    return mPermissionsManager;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState,
      @Nullable PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
    initializeViewModelBinding();
    // Ensures data binding was made.
    final ViewDataBinding binding = mViewModeHelper.getBinding();
    if (binding == null) {
      throw new IllegalStateException(
          "Binding cannot be null. Perform binding before calling getBinding()");
    }
  }

  /**
   * Call this after your view is ready - usually on the end of {@link
   * android.app.Activity#onCreate(Bundle)}
   *
   * @param view view
   */
  @SuppressWarnings("unused") public void setModelView(@NonNull final ViewInterface view) {
    mViewModeHelper.setView(view);
  }

  @Nullable public Class<ViewModelType> getViewModelClass() {
    return null;
  }

  @CallSuper @Override public void onStart() {
    super.onStart();
    mViewModeHelper.onStart();
  }

  @CallSuper @Override public void onDestroy() {
    mViewModeHelper.onDestroy(this);
    super.onDestroy();
  }

  @CallSuper @Override public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);
    mViewModeHelper.onSaveInstanceState(outState);
  }

  /**
   * @see ViewModelHelper#getViewModel()
   */
  @SuppressWarnings("unused") @NonNull public ViewModelType getViewModel() {
    return mViewModeHelper.getViewModel();
  }

  @Override public abstract ViewModelBindingConfig getViewModelBindingConfig();

  @Override public void removeViewModel() {
    mViewModeHelper.removeViewModel(this);
  }

  @SuppressWarnings({ "unused", "ConstantConditions", "unchecked" }) @NonNull
  public DataBinding getBinding() {
    try {
      return (DataBinding) mViewModeHelper.getBinding();
    } catch (ClassCastException ex) {
      throw new IllegalStateException("Method getViewModelBindingConfig() has to return same "
          + "ViewDataBinding type as it is set to base Fragment");
    }
  }

  public App getApp() {
    return (App) getApplication();
  }

  @SuppressWarnings("unchecked") @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    // Injects dependencies.
    getApp().getActivityInjector()
        .inject(
            (BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ViewDataBinding>) this);
    // Initializes activity class.
    super.onCreate(savedInstanceState);
    // Disable landscape orientation.
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    // Hide both the navigation bar and the status bar.
    View decorView = getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_IMMERSIVE
        | View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);

    // Initializes view model
    Class<? extends AbstractViewModel<ViewInterface>> viewModelClass = getViewModelClass();
    // try to extract the ViewModel class from the implementation
    if (viewModelClass == null) {
      //noinspection unchecked
      viewModelClass =
          (Class<? extends AbstractViewModel<ViewInterface>>) ProxyViewHelper.getGenericType(
              getClass(), AbstractViewModel.class);
    }
    mViewModeHelper.onCreate(this, savedInstanceState, viewModelClass, getIntent().getExtras());
    // Bind the activity to its view model.
    initializeViewModelBinding();
    // Bind views references.
    ButterKnife.bind(this);
  }

  @CallSuper @Override public void onStop() {
    super.onStop();
    mViewModeHelper.onStop();
  }

  @CallSuper @Override public void onResume() {
    super.onResume();
    if (!mSkipAuth) {
      mAuthManager.auth(this);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    for (int i = 0; i < permissions.length; i++) {
      Permission permission = Permission.fromManifest(permissions[i]);
      if (grantResults[i] != PERMISSION_GRANTED) {
        Log.w(TAG, permission.name() + " not granted.");
        onRequestPermissionsFailed(permission);
      }
    }
  }

  @Override public void toast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + getClass().getSimpleName() + " for " + getApplication().getClass()
        .getSimpleName() + "(" + getApplication().hashCode() + ")");
  }

  @SuppressWarnings("unchecked") private void initializeViewModelBinding() {
    mViewModeHelper.performBinding(this);
    getViewModel().inject(getApp().getViewModelInjector());
    mViewModeHelper.setView((ViewInterface) this);
  }
}
