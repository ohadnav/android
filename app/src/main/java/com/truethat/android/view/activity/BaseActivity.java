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
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.external.ProxyViewHelper;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;
import eu.inloop.viewmodel.ViewModelHelper;
import eu.inloop.viewmodel.base.ViewModelBaseEmptyActivity;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity<ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends ViewModelBaseEmptyActivity implements BaseViewInterface, AuthListener {
  /**
   * {@link BaseViewModel} manager of this activity.
   */
  @NonNull private final ViewModelHelper<ViewInterface, ViewModel> mViewModelHelper =
      new ViewModelHelper<>();
  /**
   * Root view layout of each activity.
   */
  @BindView(R.id.activityRootView) protected View mRootView;
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  /**
   * Whether to skip authentication.
   */
  boolean mSkipAuth = false;

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
    Log.v(TAG, "onAuthFailed");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        startActivity(new Intent(BaseActivity.this, WelcomeActivity.class));
      }
    });
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState,
      @Nullable PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
    initializeViewModel();
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

  @SuppressWarnings({ "WeakerAccess", "SameReturnValue" }) @Nullable
  public Class<ViewModel> getViewModelClass() {
    return null;
  }

  @CallSuper @Override public void onStart() {
    super.onStart();
    mViewModelHelper.onStart();
  }

  @CallSuper @Override public void onDestroy() {
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
  }

  @CallSuper @Override public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);
    mViewModelHelper.onSaveInstanceState(outState);
  }

  /**
   * @see ViewModelHelper#getViewModel()
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
      e.printStackTrace();
      throw new IllegalStateException("Method getViewModelBindingConfig() has to return same "
          + "ViewDataBinding type as it is set to base Fragment");
    }
  }

  public App getApp() {
    return (App) getApplication();
  }

  @SuppressWarnings("unchecked") @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
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
              getClass(), BaseViewModel.class);
    }
    mViewModelHelper.onCreate(this, savedInstanceState, viewModelClass, getIntent().getExtras());
    // Bind the activity to its view model.
    initializeViewModel();
    // Bind views references.
    ButterKnife.bind(this);
  }

  @CallSuper @Override public void onStop() {
    super.onStop();
    mViewModelHelper.onStop();
  }

  @CallSuper @Override public void onResume() {
    super.onResume();
    if (!mSkipAuth) {
      AppContainer.getAuthManager().auth(this);
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

  @Override public AuthListener getAuthListener() {
    return this;
  }

  @Override public BaseActivity getBaseActivity() {
    return this;
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
