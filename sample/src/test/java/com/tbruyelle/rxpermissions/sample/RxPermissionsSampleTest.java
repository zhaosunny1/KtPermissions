package com.tbruyelle.rxpermissions.sample;

import android.annotation.TargetApi;
import android.os.Build;

import com.tbruyelle.rxpermissions3.RxPermissions;

import org.junit.Before;
import org.junit.Test;

/**
 * Sample tests for {@link RxPermissions}.
 */
public class RxPermissionsSampleTest {

//    @Mock
    private RxPermissions rxPermissions;

    @Before
    public void setUp() {
//        MockitoAnnotations.initMocks(this);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void test_permission_denied_dont_ask_again() throws Exception {
        // mocks
//        final String permissionString = Manifest.permission.READ_PHONE_STATE;
//        final boolean granted = false;
//        final boolean shouldShowRequestPermissionRationale = false;
//        final Permission permission = new Permission(permissionString, granted, shouldShowRequestPermissionRationale);
//        when(rxPermissions.requestEach(permissionString)).thenReturn(Observable.just(permission));
//        // test
//        rxPermissions.requestEach(permissionString).test().assertNoErrors().assertValue(permission);
    }

}
