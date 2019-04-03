package nstv.arcoreplayground.animation

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_animation.*
import nstv.arcoreplayground.R

const val TAG = "AnimationActivity"
const val RENDERABLE_ANDY = 0


class AnimationActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment
    var andyNode: AnchorNode? = null
    var andyRenderable: ModelRenderable? = null
    var animator: ModelAnimator? = null
    private var nextAnimation: Int = 0
    val modelLoader by lazy {
        ModelLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)
        loadModels()

        arFragment = ar_fragment as ArFragment
        // When a plane is tapped, the model is placed on an Anchor node anchored to the plane.
        arFragment.setOnTapArPlaneListener(this::onPlaneTap)

        // Add a frame update listener to the scene to control the state of the buttons.
        arFragment.arSceneView.scene.addOnUpdateListener(this::onFrameUpdate)
        animateBtn.isEnabled = false
        animateBtn.setOnClickListener(this::onPlayAnimation)
    }

    fun loadModels() {
        modelLoader.loadModel(RENDERABLE_ANDY, R.raw.andy_dance)
    }

    private fun onPlayAnimation(unusedView: View) {
        if (animator?.isRunning != true) {
            val data = andyRenderable?.getAnimationData(nextAnimation)
            nextAnimation = (nextAnimation + 1) % andyRenderable!!.animationDataCount
            animator = ModelAnimator(data, andyRenderable)
            animator?.start()
            val toast = Toast.makeText(this, data?.name, Toast.LENGTH_SHORT)
            Log.d(TAG, "Starting animation %${data?.name} - %${data?.durationMs} ms long")
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun onPlaneTap(
        hitResult: HitResult,
        unusedPlane: Plane,
        unusedMotionEvent: MotionEvent
    ) {
        if (andyRenderable == null) {
            return
        }
        // Create the Anchor.
        val anchor = hitResult.createAnchor()

        if (andyNode == null) {
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val andy = SkeletonNode()

            andy.setParent(anchorNode)
            andy.renderable = andyRenderable
            andyNode = anchorNode
        }
    }

    private fun onFrameUpdate(unusedframeTime: FrameTime) {
        // If the model has not been placed yet, disable the buttons.
        if (andyNode == null) {
            if (animateBtn.isEnabled) {
                animateBtn.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.GRAY)
                animateBtn.isEnabled = false
            }
        } else if (!animateBtn.isEnabled) {
            animateBtn.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
            animateBtn.isEnabled = true

        }
    }

    fun setRenderable(id: Int, renderable: ModelRenderable) {
        when (id) {
            RENDERABLE_ANDY -> andyRenderable = renderable
        }
    }

    fun onException(id: Int, throwable: Throwable) {
        val toast = Toast.makeText(this, "Unable to load renderable: $id", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        Log.e(TAG, "Unable to load andy renderable", throwable)
    }
}
