package com.truethat.android.viewmodel;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.auth.AuthResult;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.application.storage.internal.FakeInternalStorageManager;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.empathy.FakeReactionDetectionManager;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.view.activity.MainActivity;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import com.truethat.android.viewmodel.viewinterface.ToolbarViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.Before;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class ViewModelTestSuite {
  public static final Duration DEFAULT_TIMEOUT = Duration.ONE_HUNDRED_MILLISECONDS;
  final MockWebServer mMockWebServer = new MockWebServer();
  protected FakeAuthManager mFakeAuthManager;
  protected FakeInternalStorageManager mFakeInternalStorageManager;
  protected FakeReactionDetectionManager mFakeReactionDetectionManager;
  protected FakeDeviceManager mFakeDeviceManager;
  protected String mLastRequest;
  Date mNow;

  @SuppressWarnings("unchecked") @Before public void setUp() throws Exception {
    mNow = new Date();
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(DEFAULT_TIMEOUT);
    Awaitility.setDefaultPollDelay(new Duration(10, TimeUnit.MILLISECONDS));
    Awaitility.setDefaultPollInterval(new Duration(10, TimeUnit.MILLISECONDS));
    // Starts mock server
    mMockWebServer.start(8070);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mLastRequest = request.getBody().readUtf8();
        return new MockResponse();
      }
    });
    NetworkUtil.setBackendUrl(BuildConfig.TEST_BASE_BACKEND_URL);

    // Sets up mocks
    AppContainer.setPermissionsManager(new PermissionsManager() {
      @Override public boolean isPermissionGranted(Permission permission) {
        return true;
      }

      @Override public void requestIfNeeded(Activity activity, Permission permission) {

      }
    });
    mFakeDeviceManager = new FakeDeviceManager("android-unit-test", "+1911");
    AppContainer.setDeviceManager(mFakeDeviceManager);
    mFakeInternalStorageManager = new FakeInternalStorageManager();
    AppContainer.setInternalStorageManager(mFakeInternalStorageManager);
    mFakeAuthManager = new FakeAuthManager(mFakeDeviceManager, mFakeInternalStorageManager);
    AppContainer.setAuthManager(mFakeAuthManager);
    mFakeReactionDetectionManager = new FakeReactionDetectionManager();
    AppContainer.setReactionDetectionManager(mFakeReactionDetectionManager);
    // Signs a user up.
    mFakeAuthManager.signUp(new UnitTestViewInterface(), new User(mFakeDeviceManager));
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }

  @After public void tearDown() throws Exception {
    mMockWebServer.close();
  }

  @SuppressWarnings("unchecked")
  <ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>> ViewModel createViewModel(
      Class<ViewModel> viewModelTypeClass, ViewInterface viewInterface,
      @Nullable Bundle savedInstanceState) throws Exception {
    ViewModel viewModel = viewModelTypeClass.newInstance();
    viewModel.onCreate(null, savedInstanceState);
    viewModel.onBindView(viewInterface);
    return viewModel;
  }

  @SuppressWarnings("unused") class UnitTestViewInterface
      implements AuthListener, BaseFragmentViewInterface, ToolbarViewInterface {
    private String mToastText;
    private AuthResult mAuthResult;
    private boolean mIsVisible = true;
    private boolean mIsToolbarVisible = true;
    private int mMainPagerIndex = MainActivity.sLaunchIndex;

    public String getToastText() {
      return mToastText;
    }

    public AuthResult getAuthResult() {
      return mAuthResult;
    }

    @Override public void onAuthOk() {
      mAuthResult = AuthResult.OK;
    }

    @Override public void onAuthFailed() {
      mAuthResult = AuthResult.FAILED;
    }

    @Override public void toast(String text) {
      mToastText = text;
    }

    @Override public BaseActivity getBaseActivity() {
      return null;
    }

    @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
      return null;
    }

    @Override public void removeViewModel() {

    }

    @Override public boolean isVisibleAndResumed() {
      return mIsVisible;
    }

    @Override public String getTAG() {
      return this.getClass().getSimpleName();
    }

    @Override public void navigateToTheater() {
      mMainPagerIndex = MainActivity.TOOLBAR_THEATER_INDEX;
    }

    @Override public void navigateToStudio() {
      mMainPagerIndex = MainActivity.TOOLBAR_STUDIO_INDEX;
    }

    @Override public void navigateToRepertoire() {
      mMainPagerIndex = MainActivity.TOOLBAR_REPERTOIRE_INDEX;
    }

    @Override public void hideToolbar() {
      mIsToolbarVisible = false;
    }

    @Override public void showToolbar() {
      mIsToolbarVisible = true;
    }

    int getMainPagerIndex() {
      return mMainPagerIndex;
    }

    boolean isToolbarVisible() {
      return mIsToolbarVisible;
    }
  }
}
