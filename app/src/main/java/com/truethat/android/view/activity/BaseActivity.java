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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.App;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.external.viewmodel.ProxyViewHelper;
import com.truethat.android.external.viewmodel.ViewModelHelper;
import com.truethat.android.external.viewmodel.ViewModelProvider;
import com.truethat.android.view.fragment.VisibilityListener;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseListener;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity<ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends AppCompatActivity implements BaseViewInterface, BaseListener, VisibilityListener {
  /**
   * {@link BaseViewModel} manager of this activity.
   */
  @NonNull private final ViewModelHelper<ViewInterface, ViewModel> mViewModelHelper =
      new ViewModelHelper<>();
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  @Nullable private ViewModelProvider mViewModelProvider;

  @Nullable public ViewModelProvider getViewModelProvider() {
    return mViewModelProvider;
  }

  /**
   * Permission not granted callback.
   *
   * @param permission that was just rejected.
   */
  @MainThread public void onPermissionRejected(Permission permission) {
    Log.w(TAG, "Permission " + permission + " rejected.");
    Intent askForPermission = new Intent(this, AskForPermissionActivity.class);
    askForPermission.putExtra(AskForPermissionActivity.EXTRA_PERMISSION, permission);
    startActivityForResult(askForPermission, permission.getRequestCode());
  }

  /**
   * Permission granted callback.
   *
   * @param permission that was just granted.
   */
  @MainThread public void onPermissionGranted(Permission permission) {
    Log.d(TAG, "Permission " + permission + " granted.");
    getViewModel().onPermissionGranted(permission);
  }

  @CallSuper @Override public void onCreate(@Nullable Bundle savedInstanceState,
      @Nullable PersistableBundle persistentState) {
    Log.d(TAG, "onCreate(Bundle, PersistableBundle)");
    super.onCreate(savedInstanceState, persistentState);
    initializeViewModel();
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    Log.d(TAG, "onRestoreInstanceState");
    super.onRestoreInstanceState(savedInstanceState);
    getViewModel().onRestoreInstanceState(savedInstanceState);
  }

  /**
   * Call this after your view is ready - usually on the end of {@link
   * android.app.Activity#onCreate(Bundle)}
   *
   * @param view view
   */
  @SuppressWarnings("unused") public void setModelView(@NonNull final ViewInterface view) {
    mViewModelHelper.setView(view);
  }

  /**
   * @see eu.inloop.viewmodel.ViewModelHelper#getViewModel()
   */
  @SuppressWarnings("unused") @NonNull public ViewModel getViewModel() {
    return mViewModelHelper.getViewModel();
  }

  @Override public abstract ViewModelBindingConfig getViewModelBindingConfig();

  @Override public void removeViewModel() {
    mViewModelHelper.removeViewModel(this);
  }

  @SuppressWarnings({ "unused", "ConstantConditions", "unchecked" }) @NonNull
  public DataBinding getBinding() {
    try {
      return (DataBinding) mViewModelHelper.getBinding();
    } catch (ClassCastException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
      throw new IllegalStateException("Method getViewModelBindingConfig() has to return same "
          + "ViewDataBinding type as it is set to base Fragment");
    }
  }

  public App getApp() {
    return (App) getApplication();
  }

  @SuppressWarnings("unchecked") @Override @CallSuper
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreate(Bundle)");
    //This code must be execute prior to super.onCreate()
    mViewModelProvider = ViewModelProvider.newInstance(this);
    // Initializes activity class.
    super.onCreate(savedInstanceState);
    // Disable landscape orientation.
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    // Hide both the navigation bar and the status bar.
    getWindow().getDecorView()
        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    // Initializes view model
    Class<ViewModel> viewModelClass =
        (Class<ViewModel>) ProxyViewHelper.getGenericType(getClass(), BaseViewModel.class);
    mViewModelHelper.onCreate(this, savedInstanceState, viewModelClass, getIntent().getExtras());
    // Bind the activity to its view model.
    initializeViewModel();
    // Bind views references.
    ButterKnife.bind(this);
    if (!BuildConfig.DEBUG) {
      Appsee.start(BuildConfig.APPSEE_API_KEY);
    }
  }

  @CallSuper @Override public void onStart() {
    Log.d(TAG, "onStart");
    super.onStart();
    mViewModelHelper.onStart();
  }

  @CallSuper @Override public void onStop() {
    Log.d(TAG, "onStop");
    super.onStop();
    if (isFinishing()) {
      if (null == mViewModelProvider) {
        throw new IllegalStateException(
            "ViewModelProvider for activity " + this + " was null."); //NON-NLS
      }
      mViewModelProvider.removeAllViewModels();
    }
    mViewModelHelper.onStop();
  }

  @CallSuper @Override public void onDestroy() {
    Log.d(TAG, "onDestroy");
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
  }

  @CallSuper @Override public void onSaveInstanceState(@NonNull final Bundle outState) {
    Log.d(TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
    mViewModelHelper.onSaveInstanceState(outState);
  }

  @Override public boolean shouldBeVisible(Object o) {
    if (o instanceof Fragment || o instanceof View) {
      int id = o instanceof Fragment ? ((Fragment) o).getId() : ((View) o).getId();
      return getApp().isResumed(this) && findViewById(id) != null;
    }
    return false;
  }

  @Override public void toast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  @Override public BaseActivity getBaseActivity() {
    return this;
  }

  @Override public String getTAG() {
    return TAG;
  }

  @Override public String toString() {
    return TAG;
  }

  @Override protected void onPause() {
    Log.d(TAG, "onPause");
    super.onPause();
    getViewModel().onPause();
  }

  @CallSuper @Override public void onResume() {
    Log.d(TAG, "onResume");
    super.onResume();
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.ACTIVITY.name(), TAG);
    }
    getViewModel().onResume();
  }

  @Override @Nullable public Object onRetainCustomNonConfigurationInstance() {
    return mViewModelProvider;
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    Log.d(TAG, "onRequestPermissionsResult");
    for (int i = 0; i < permissions.length; i++) {
      Permission permission = Permission.fromManifest(permissions[i]);
      if (grantResults[i] != PERMISSION_GRANTED) {
        onPermissionRejected(permission);
      } else if (grantResults[i] == PERMISSION_GRANTED) {
        onPermissionGranted(permission);
      }
    }
  }

  /**
   * Initialize data-view binding for this activity, and injects dependencies to the view model.
   */
  @SuppressWarnings("unchecked") private void initializeViewModel() {
    mViewModelHelper.performBinding(this);
    mViewModelHelper.setView((ViewInterface) this);
    // Ensures data binding was made.
    if (mViewModelHelper.getBinding() == null) {
      throw new IllegalStateException("Binding cannot be null.");
    }
    // Sets up context
    mViewModelHelper.getViewModel().setContext(this);
  }
}
