package nstv.arcoreplayground

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import nstv.arcoreplayground.andy.AndyActivity
import nstv.arcoreplayground.animation.AnimationActivity
import nstv.arcoreplayground.augmentedFace.AugmentedFaceActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_andy.setOnClickListener { startActivity(Intent(this, AndyActivity::class.java)) }
        btn_aface.setOnClickListener { startActivity(Intent(this, AugmentedFaceActivity::class.java)) }
        btn_animate.setOnClickListener { startActivity(Intent(this, AnimationActivity::class.java)) }

    }
}
