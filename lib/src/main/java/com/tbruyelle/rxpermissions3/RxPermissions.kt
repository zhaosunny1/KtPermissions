/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tbruyelle.rxpermissions3

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf

class RxPermissions {
    @VisibleForTesting
    var mRxPermissionsFragment: Lazy<RxPermissionsFragment>


    var permissions: ArrayList<String> = ArrayList()
    var resultPermission: ArrayList<Permission> = ArrayList()
    var sharedFlow: MutableSharedFlow<Permission> = MutableSharedFlow()

    constructor(activity: FragmentActivity) {
        mRxPermissionsFragment = getLazySingleton(activity.supportFragmentManager)
        permissions = ArrayList()
        resultPermission = ArrayList()
        sharedFlow = MutableSharedFlow()
    }

    constructor(fragment: Fragment) {
        mRxPermissionsFragment = getLazySingleton(fragment.childFragmentManager)
        permissions.clear()
        permissions = ArrayList()
        resultPermission = ArrayList()
        sharedFlow = MutableSharedFlow()
    }

    private fun getLazySingleton(fragmentManager: FragmentManager): Lazy<RxPermissionsFragment> {
        return object : Lazy<RxPermissionsFragment> {
            private var rxPermissionsFragment: RxPermissionsFragment? = null

            @Synchronized
            override fun get(): RxPermissionsFragment {
                if (rxPermissionsFragment == null) {
                    rxPermissionsFragment = getRxPermissionsFragment(fragmentManager)
                }
                return rxPermissionsFragment!!
            }
        }
    }

    private fun getRxPermissionsFragment(fragmentManager: FragmentManager): RxPermissionsFragment? {
        var rxPermissionsFragment = findRxPermissionsFragment(fragmentManager)
        val isNewInstance = rxPermissionsFragment == null
        if (isNewInstance) {
            rxPermissionsFragment = RxPermissionsFragment()
            fragmentManager
                .beginTransaction()
                .add(rxPermissionsFragment, TAG)
                .commitNow()
        }
        return rxPermissionsFragment
    }

    private fun findRxPermissionsFragment(fragmentManager: FragmentManager): RxPermissionsFragment? {
        return fragmentManager.findFragmentByTag(TAG) as RxPermissionsFragment?
    }

    fun setLogging(logging: Boolean) {
        mRxPermissionsFragment.get().setLogging(logging)
    }


    /**
     * Request permissions immediately, **must be invoked during initialization phase
     * of your application**.
     */


    @Suppress("unused")
    suspend fun request(
        permissions: Array<String>,
        invoked: RequestInvoke = RequestInvoke { },
        granted: RequestGranted,
    ) {
        sharedFlow = MutableSharedFlow(extraBufferCapacity = permissions.size + 2)
        requestImplementation(*permissions)
        sharedFlow.collect {
            Log.e(TAG, "收到权限: " + it.name + " mask=" + it.mask)
            if (it.mask == 0) {
                resultPermission.clear()
            } else if (it.mask == -1) {
                resultPermission.add(it)
            } else if (it.mask == 1) {
                val s = resultPermission.filter { i -> !i.granted }
                if (s.isEmpty()) {
                    granted.onGranted()
                } else {
                    invoked.onInvoked(s.toTypedArray())
                }
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    private suspend fun requestImplementation(vararg permissions: String) {
        val unrequestedPermissions: MutableList<String> = ArrayList()

        // In case of multiple permissions, we create an Observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (permission in permissions) {
            mRxPermissionsFragment.get().log("Requesting permission $permission")
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                mRxPermissionsFragment.get().log(" permission $permission is Granted")
                sharedFlow.emit(Permission(permission, true, false))
            }
            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                mRxPermissionsFragment.get().log(" permission $permission is Revoked")
                sharedFlow.emit(Permission(permission, false, false))
            }
            var subject: Flow<Permission>? =
                mRxPermissionsFragment.get().getSubjectByPermission(permission)
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission)
                mRxPermissionsFragment.get().setSubjectForPermission(permission, sharedFlow)
            }
        }
        if (!unrequestedPermissions.isEmpty()) {
            val unrequestedPermissionsArray = unrequestedPermissions.toTypedArray()
            requestPermissionsFromFragment(unrequestedPermissionsArray)
        }
    }

    /**
     * Invokes Activity.shouldShowRequestPermissionRationale and wraps
     * the returned value in an observable.
     *
     *
     * In case of multiple permissions, only emits true if
     * Activity.shouldShowRequestPermissionRationale returned true for
     * all revoked permissions.
     *
     *
     * You shouldn't call this method if all permissions have been granted.
     *
     *
     * For SDK &lt; 23, the observable will always emit false.
     */
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        vararg permissions: String,
    ): Flow<Boolean> {
        return if (!isMarshmallow) {
            flowOf(false)
        } else flowOf(shouldShowRequestPermissionRationaleImplementation(activity, *permissions))
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestPermissionRationaleImplementation(
        activity: Activity,
        vararg permissions: String,
    ): Boolean {
        for (p in permissions) {
            if (!isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsFromFragment(permissions: Array<String>?) {
//        mRxPermissionsFragment.get()
//            .log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions))
        mRxPermissionsFragment.get().requestPermissions(permissions!!)
    }

    /**
     * Returns true if the permission is already granted.
     *
     *
     * Always true if SDK &lt; 23.
     */
    fun isGranted(permission: String?): Boolean {
        return !isMarshmallow || mRxPermissionsFragment.get().isGranted(permission)
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     *
     *
     * Always false if SDK &lt; 23.
     */
    fun isRevoked(permission: String?): Boolean {
        return isMarshmallow && mRxPermissionsFragment.get().isRevoked(permission)
    }

    val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        mRxPermissionsFragment.get()
            .onRequestPermissionsResult(permissions, grantResults, BooleanArray(permissions.size))
    }

    fun interface Lazy<V> {
        fun get(): V
    }

    companion object {
        val TAG = RxPermissions::class.java.simpleName
        val TRIGGER = Any()
    }
}