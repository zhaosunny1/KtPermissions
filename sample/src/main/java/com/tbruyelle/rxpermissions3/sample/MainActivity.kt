package com.tbruyelle.rxpermissions3.sample

import android.Manifest
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.zhaosunny.myapplication.R
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val camera: Camera? = null
    private val surfaceView: SurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        findViewById<View>(R.id.enableCamera).setOnClickListener {
            val rxPermissions = RxPermissions(this@MainActivity)
            rxPermissions.setLogging(true)
            rxPermissions.request(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO
            ) {

            }

        }

    } //        setContentView(R.layout.act_main);

    //        surfaceView = findViewById(R.id.surfaceView);
    //
    //        disposable = RxView.clicks(findViewById(R.id.enableCamera))
    //                // Ask for permissions when button is clicked
    //                .compose(rxPermissions.ensureEach(permission.CAMERA))
    //                .subscribe(new Consumer<Permission>() {
    //                               @Override
    //                               public void accept(Permission permission) {
    //                                   Log.i(TAG, "Permission result " + permission);
    //                                   if (permission.granted) {
    //                                       releaseCamera();
    //                                       camera = Camera.open(0);
    //                                       try {
    //                                           camera.setPreviewDisplay(surfaceView.getHolder());
    //                                           camera.startPreview();
    //                                       } catch (IOException e) {
    //                                           Log.e(TAG, "Error while trying to display the camera preview", e);
    //                                       }
    //                                   } else if (permission.shouldShowRequestPermissionRationale) {
    //                                       // Denied permission without ask never again
    //                                       Toast.makeText(MainActivity.this,
    //                                               "Denied permission without ask never again",
    //                                               Toast.LENGTH_SHORT).show();
    //                                   } else {
    //                                       // Denied permission with ask never again
    //                                       // Need to go to the settings
    //                                       Toast.makeText(MainActivity.this,
    //                                               "Permission denied, can't enable the camera",
    //                                               Toast.LENGTH_SHORT).show();
    //                                   }
    //                               }
    //                           },
    //                        new Consumer<Throwable>() {
    //                            @Override
    //                            public void accept(Throwable t) {
    //                                Log.e(TAG, "onError", t);
    //                            }
    //                        },
    //                        new Action() {
    //                            @Override
    //                            public void run() {
    //                                Log.i(TAG, "OnComplete");
    //                            }
    //                        });
    //    }
    //
    //    @Override
    //    protected void onDestroy() {
    //        if (disposable != null && !disposable.isDisposed()) {
    //            disposable.dispose();
    //        }
    //        super.onDestroy();
    //    }
    //
    //    @Override
    //    protected void onStop() {
    //        super.onStop();
    //        releaseCamera();
    //    }
    //
    //    private void releaseCamera() {
    //        if (camera != null) {
    //            camera.release();
    //            camera = null;
    //        }
    //    }
    companion object {
        private const val TAG = "RxPermissionsSample"
    }
}