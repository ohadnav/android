package com.truethat.android.viewmodel;

import android.support.annotation.Nullable;
import android.test.mock.MockContext;
import com.google.gson.Gson;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.di.component.AppInjectorComponent;
import com.truethat.android.di.component.DaggerAppInjectorComponent;
import com.truethat.android.di.component.DaggerUnitTestComponent;
import com.truethat.android.di.component.UnitTestComponent;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.fake.FakeAuthModule;
import com.truethat.android.di.module.fake.FakeDeviceModule;
import com.truethat.android.di.module.fake.FakeInternalStorageModule;
import com.truethat.android.di.module.fake.FakeReactionDetectionModule;
import com.truethat.android.di.module.fake.MockAppModule;
import com.truethat.android.empathy.FakeReactionDetectionManager;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class ViewModelTestSuite {
  final MockWebServer mMockWebServer = new MockWebServer();
  public FakeAuthManager mFakeAuthManager;
  FakeReactionDetectionManager mFakeReactionDetectionManager;
  Gson mGson;
  UnitTestComponent mUnitTestComponent;
  Date mNow;
  private AppInjectorComponent mInjector;

  @SuppressWarnings("unchecked") @Before public void setUp() throws Exception {
    mNow = new Date();
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(Duration.ONE_HUNDRED_MILLISECONDS);
    Awaitility.setDefaultPollDelay(new Duration(10, TimeUnit.MILLISECONDS));
    Awaitility.setDefaultPollInterval(new Duration(10, TimeUnit.MILLISECONDS));
    // Starts mock server
    mMockWebServer.start(8080);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse();
      }
    });
    // Initializes app component for future injections.
    mUnitTestComponent =
        DaggerUnitTestComponent.builder()
            .mockAppModule(new MockAppModule(new MockContext()))
            // Sets the backend URL, for MockWebServer.
            .netModule(new NetModule(BuildConfig.TEST_BASE_BACKEND_URL))
            .fakeAuthModule(new FakeAuthModule())
            .fakeDeviceModule(new FakeDeviceModule())
            .fakeInternalStorageModule(new FakeInternalStorageModule())
            .fakeReactionDetectionModule(new FakeReactionDetectionModule())
            .build();
    // Initializes injected dependencies.
    mInjector = DaggerAppInjectorComponent.builder().appComponent(mUnitTestComponent).build();
    // Initialize fakes by creating a view model.
    createViewModel(BaseViewModel.class, new UnitTestViewInterface());
  }

  @After public void tearDown() throws Exception {
    mMockWebServer.close();
  }

  @SuppressWarnings("unchecked")
  <ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>> ViewModel createViewModel(
      Class<ViewModel> viewModelTypeClass, ViewInterface viewInterface) throws Exception {
    ViewModel viewModel = viewModelTypeClass.newInstance();
    viewModel.onCreate(null, null);
    viewModel.onBindView(viewInterface);
    mInjector.inject((BaseViewModel<BaseViewInterface>) viewModel);
    viewModel.onInjected();
    // Updates modules
    mFakeAuthManager = (FakeAuthManager) viewModel.getAuthManager();
    FakeDeviceManager fakeDeviceManager = (FakeDeviceManager) viewModel.getDeviceManager();
    mFakeReactionDetectionManager = (FakeReactionDetectionManager) viewModel.getReactionDetectionManager();
    mGson = viewModel.getGson();
    // Sign up
    mFakeAuthManager.signUp(new UnitTestViewInterface(),
        new User(fakeDeviceManager.getDeviceId(), fakeDeviceManager.getPhoneNumber()));
    return viewModel;
  }

  class UnitTestViewInterface implements AuthListener, BaseViewInterface {
    @Override public void onAuthOk() {

    }

    @Override public void onAuthFailed() {

    }

    @Override public void toast(String text) {

    }

    @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
      return null;
    }

    @Override public void removeViewModel() {

    }
  }
}
