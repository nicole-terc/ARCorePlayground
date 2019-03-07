package nstv.arcoreplayground.augmentedFace

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlinx.android.synthetic.main.activity_augmented_face.*
import nstv.arcoreplayground.R
import java.util.*


class AugmentedFaceActivity : AppCompatActivity() {

    val TAG = "AugmentedFaceActivity"
    val MIN_OPENGL_VERSION = 3.0

    var glassesRenderable: ModelRenderable? = null
    val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_face)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        loadModels()
    }

    private fun loadModels() {

        ModelRenderable.builder()
            .setSource(this, R.raw.sunglasses)
            .build()
            .thenAccept { modelRenderable ->
                glassesRenderable = modelRenderable
                modelRenderable.isShadowCaster = false
                modelRenderable.isShadowReceiver = false
            }

        buildScene()
    }

    private fun buildScene() {

        val sceneView = (ar_fragment as AugmentedFaceFragment).arSceneView

        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val scene = sceneView.scene

        scene.addOnUpdateListener {
            if (glassesRenderable == null) {
                return@addOnUpdateListener
            }

            val faceList = sceneView.session?.getAllTrackables(AugmentedFace::class.java) ?: Collections.emptyList()

            // Make new AugmentedFaceNodes for any new faces.
            for (face in faceList) {
                if (!faceNodeMap.containsKey(face)) {
                    val faceNode = AugmentedFaceNode(face)

                    val left = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT)
                    val right = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT)
                    val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
                    val center = face.centerPose

                    Log.d("POSE", "left: $left")
                    Log.d("POSE", "right: $right")
                    Log.d("POSE", "nose: $nose")
                    Log.d("POSE", "center: $center")

                    val node = Node()

                    node.localScale = Vector3(0.6f * getDistance(left, right), 0.04f, 0.045f)
                    node.renderable = glassesRenderable?.makeCopy()
                    node.setParent(faceNode)

                    faceNode.setParent(scene)
                    faceNodeMap[face] = faceNode

                }
            }
        }

        // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
        val iterator = faceNodeMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val face = entry.key
            if (face.trackingState == TrackingState.STOPPED) {
                val node = entry.value
                node.children.forEach {
                    node.removeChild(it)
                }
                node.setParent(null)
                iterator.remove()
            }
        }
    }

    fun getDistance(a: Pose, b: Pose): Float {
        val x = Math.pow((a.tx() - b.tx()).toDouble(), 2.0)
        val y = Math.pow((a.ty() - b.ty()).toDouble(), 2.0)
        val z = Math.pow((a.tz() - b.tz()).toDouble(), 2.0)
        return Math.sqrt(x + y + z).toFloat()
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * Finishes the activity if Sceneform can not run
     */
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
}
