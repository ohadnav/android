package com.truethat.android.di.component;

import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.fake.FakeAuthModule;
import com.truethat.android.di.module.fake.FakeReactionDetectionModule;
import com.truethat.android.di.module.fake.MockAppModule;
import com.truethat.android.di.module.fake.MockPermissionsModule;
import com.truethat.android.di.scope.AppScope;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 *
 * Container component for application scoped modules, in their unit-test flavour, so that it can be used in a JUnit testing suite.
 */

@Component(modules = {
    NetModule.class, MockAppModule.class, AuthModule.class, FakeAuthModule.class,
    FakeReactionDetectionModule.class, MockPermissionsModule.class
}) @Singleton @AppScope public interface UnitTestComponent extends AppComponent {
}
