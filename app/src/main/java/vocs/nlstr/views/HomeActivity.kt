package vocs.nlstr.views

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_home.*
import vocs.nlstr.R
import vocs.nlstr.utils.AnimationEffectTypes
import vocs.nlstr.utils.InterpolatorBounceUtil
import vocs.nlstr.utils.inTransaction
import java.util.*


class HomeActivity : AppCompatActivity() {

    var isCommandshown: Boolean = false
    var isListening: Boolean = true
    var matches = ArrayList<String>()
    var DEFAULT_HEIGHT = 30

    var leftKeyword = "ok"
    var rightKeyword = "no"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager.inTransaction { add(R.id.fragContainerHome, MainListFragment()) }
        configListeners()
        setKeyWords()
    }

    //---------- NO HEREDADOS -------------------//
    private fun configListeners() {
        left_fab.setOnClickListener {
            AnimationEffect(it, AnimationEffectTypes.BUBBLE)
            //TODO: mostrar icono/info/animacion de reconocimiento en marcha
            isListening = !isListening
            hideCommands()
        }

        right_fab.setOnClickListener {
            AnimationEffect(it, AnimationEffectTypes.BUBBLE)

            showCommands(matches)
        }
    }

    private fun setKeyWords() {
        matches.add(leftKeyword)
        matches.add(rightKeyword)
    }

    private fun initView() {

    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragContainerHome)
    }

    fun startReconAnimation() {
        waveHeader.run { }
    }

    fun stopReconAnimation() {
        waveHeader.stop()
    }

    fun activateKeyWordAction(keyword: String) {
        when (keyword) {
            leftKeyword -> left_fab.isFocused
            rightKeyword -> right_fab.isFocused
        }
    }

    fun showMessage(msg: String, isStopRequired: Boolean = false) {
        tv_message.text = msg
        left_fab.requestFocus()

        var fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 1200
        fadeIn.fillAfter = true

        if (!isStopRequired) {
            var fadeOut = AlphaAnimation(1.0f, 0.0f)
            tv_message.startAnimation(fadeIn)
            tv_message.startAnimation(fadeOut)
            fadeOut.duration = 1200
            fadeOut.fillAfter = true
            fadeOut.startOffset = calculateFadeInOut(msg) + fadeIn.startOffset
        }
    }

    fun calculateFadeInOut(msgToShow: String): Long {
        return ((msgToShow.length * 300) + 500).toLong()
    }

    fun AnimationEffect(view: View, animationEffect: AnimationEffectTypes) {
        when (animationEffect) {
            AnimationEffectTypes.BUBBLE -> {
                val bubbleAnim = loadAnimation(this, R.anim.bounce)
                bubbleAnim.interpolator = InterpolatorBounceUtil(AnimationEffectTypes.BUBBLE)
                view.startAnimation(bubbleAnim)
            }

            AnimationEffectTypes.SINK_DOWN -> {
                val anim = ValueAnimator.ofInt(view.measuredHeight, 1200)
                anim.addUpdateListener { valueAnimator ->
                    val sinkAnim = valueAnimator.animatedValue as Int
                    val layoutParams = view.layoutParams
                    layoutParams.height = sinkAnim
                    view.layoutParams = layoutParams
                }
                anim.duration = 500
                anim.start()
            }

            AnimationEffectTypes.SINK_UP -> {
                val anim = ValueAnimator.ofInt(view.measuredHeight, 300)
                anim.addUpdateListener { valueAnimator ->
                    val sinkAnim = valueAnimator.animatedValue as Int
                    val layoutParams = view.layoutParams
                    layoutParams.height = sinkAnim
                    view.layoutParams = layoutParams
                }
                anim.duration = 1000
                anim.start()
            }
        }
    }

    fun showCommands(commands: ArrayList<String>) {
        var commandAdapter = ArrayAdapter<String>(this, R.layout.item_list_command_simple, commands)
        lv_commList.visibility = View.VISIBLE
        lv_commList.adapter = commandAdapter

        AnimationEffect(waveHeader, AnimationEffectTypes.SINK_DOWN)

        (getCurrentFragment() as MainListFragment).onDestroy()
        (getCurrentFragment() as MainListFragment).onResume()
        isCommandshown = true
    }

    fun hideCommands() {
        AnimationEffect(waveHeader, AnimationEffectTypes.SINK_UP)
        lv_commList.visibility = View.GONE

        (getCurrentFragment() as MainListFragment).onDestroy()
        (getCurrentFragment() as MainListFragment).onResume()
        isCommandshown = false
    }
}


