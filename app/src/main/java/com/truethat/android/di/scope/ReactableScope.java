package com.truethat.android.di.scope;

import com.truethat.android.view.fragment.ReactableFragment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 *
 * Per {@link ReactableFragment} scope.
 */

@Retention(RetentionPolicy.RUNTIME) @Scope public @interface ReactableScope {
}
