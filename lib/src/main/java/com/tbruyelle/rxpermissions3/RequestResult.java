package com.tbruyelle.rxpermissions3;

/**
 * @author zhaoyang 2023/10/10
 */
public abstract class RequestResult {
    abstract void onGranted();

    void onInvoked(Permission[] array) {
    }

}
