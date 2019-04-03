package nstv.arcoreplayground.augmentedFace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
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
import nstv.arcoreplayground.common.checkIsSupportedDeviceOrFinish
import nstv.arcoreplayground.common.takePhoto
import java.io.File
import java.util.*


class AugmentedFaceActivity : AppCompatActivity() {


    var glassesRenderable: ModelRenderable? = null
    val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_face)
        if (!checkIsSupportedDeviceOrFinish()) {
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

            sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
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

            // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
            val iterator = faceNodeMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val face = entry.key
                if (face.trackingState != TrackingState.TRACKING) {
                    val node = entry.value
                    node.setParent(null)
                    iterator.remove()
                }
            }
        }

        action_take_photo.setOnClickListener { sceneView.takePhoto { photoSaved(it) } }
    }

    fun photoSaved(filename: String) {
        val snackbar = Snackbar.make(
            content,
            "Photo saved", Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Open in Photos") {
            val photoFile = File(filename)

            val photoURI = FileProvider.getUriForFile(
                this,
                this.packageName + ".nstv.provider",
                photoFile
            )
            val intent = Intent(Intent.ACTION_VIEW, photoURI)
            intent.setDataAndType(photoURI, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }
        snackbar.show()
    }

    fun getDistance(a: Pose, b: Pose): Float {
        val x = Math.pow((a.tx() - b.tx()).toDouble(), 2.0)
        val y = Math.pow((a.ty() - b.ty()).toDouble(), 2.0)
        val z = Math.pow((a.tz() - b.tz()).toDouble(), 2.0)
        return Math.sqrt(x + y + z).toFloat()
    }
}
