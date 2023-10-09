package com.tbruyelle.rxpermissions3

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class RxPermissionsFragment : Fragment(), CoroutineScope by MainScope() {
    // Contains all the current permission requests.
    // Once granted or denied, they are removed from it.
    private val mSubjects: MutableMap<String, MutableSharedFlow<Permission>> = HashMap()
    private var mLogging = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<String>) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSIONS_REQUEST_CODE) return
        val shouldShowRequestPermissionRationale = BooleanArray(permissions.size)
        for (i in permissions.indices) {
            shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(
                permissions[i]
            )
        }
        onRequestPermissionsResult(
            permissions,
            grantResults,
            shouldShowRequestPermissionRationale
        )

    }

    fun onRequestPermissionsResult(
        permissions: Array<String>,
        grantResults: IntArray,
        shouldShowRequestPermissionRationale: BooleanArray,
    ) {
        launch {
            var i = 0
            val size = permissions.size
            while (i < size) {
                log("onRequestPermissionsResult  " + permissions[i])
                // Find the corresponding subject
                val subject: MutableSharedFlow<Permission>? = mSubjects[permissions[i]]
                if (subject == null) {
                    // No subject found
                    Log.e(
                        RxPermissions.TAG,
                        "RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request."
                    )
                    return@launch
                }

                val granted = grantResults[i] == PackageManager.PERMISSION_GRANTED
                subject.emit(
                    Permission(
                        permissions[i],
                        granted,
                        shouldShowRequestPermissionRationale[i]
                    )
                )

                i++
            }
            val p = Permission("End", false)
            p.mask = 1
            mSubjects[permissions[0]]?.emit(p)
            mSubjects.clear()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isGranted(permission: String?): Boolean {
        val fragmentActivity = activity
            ?: throw IllegalStateException("This fragment must be attached to an activity.")
        return fragmentActivity.checkSelfPermission(permission!!) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @TargetApi(Build.VERSION_CODES.M)
    fun isRevoked(permission: String?): Boolean {
        val fragmentActivity = activity
            ?: throw IllegalStateException("This fragment must be attached to an activity.")
        return fragmentActivity.packageManager.isPermissionRevokedByPolicy(
            permission!!,
            activity!!.packageName
        )
    }

    fun setLogging(logging: Boolean) {
        mLogging = logging
    }

    fun getSubjectByPermission(permission: String): Flow<Permission>? {
        return mSubjects[permission]
    }

    fun setSubjectForPermission(permission: String, subject: MutableSharedFlow<Permission>) {
        mSubjects[permission] = subject
    }

    fun log(message: String?) {
        if (mLogging) {
            Log.d(RxPermissions.TAG, message!!)
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 42
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}