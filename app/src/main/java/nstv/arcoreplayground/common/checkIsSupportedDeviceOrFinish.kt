package nstv.arcoreplayground.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import nstv.arcoreplayground.andy.AndyActivity
import nstv.arcoreplayground.augmentedFace.AugmentedFaceActivity

/**
 * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
 * on this device.
 *
 * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
 *
 * Finishes the activity if Sceneform can not run
 */

fun AugmentedFaceActivity.checkIsSupportedDeviceOrFinish() = checkIsSupportedDeviceOrFinish(this)
fun AndyActivity.checkIsSupportedDeviceOrFinish() = checkIsSupportedDeviceOrFinish(this)


const val TAG = "checkIsSupportedDeviceOrFinish"
const val MIN_OPENGL_VERSION = 3.0

private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
    if (ArCoreApk.getInstance().checkAvailability(activity) === ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
        Log.e(TAG, "Augmented Faces requires ArCore.")
        Toast.makeText(activity, "Augmented Faces requires ArCore", Toast.LENGTH_LONG).show()
        activity.finish()
        return false
    }
    val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .deviceConfigurationInfo
        .glEsVersion
    if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
        Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
        Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
            .show()
        activity.finish()
        return false
    }
    return true
}
