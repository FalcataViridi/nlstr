package vocs.nlstr.views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.animation.AlphaAnimation
import kotlinx.android.synthetic.main.activity_home.*
import vocs.nlstr.R
import vocs.nlstr.utils.inTransaction
import java.util.ArrayList

class HomeActivity : AppCompatActivity() {

    var isListening: Boolean = true
    var isCommand: Boolean = false
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
        right_fab.setOnClickListener {

        }

        left_fab.setOnClickListener {
            //TODO: mostrar icono/info/animacion de reconocimiento en marcha
            isListening = !isListening
        }
    }

    private fun setKeyWords() {
        matches.add(leftKeyword)
        matches.add(rightKeyword)
    }

    private fun initView() {
    }

    fun getCurrentFragment(): Fragment {
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

    fun showMessage(msg: String, isStopRequired: Boolean) {

        tv_message.text = msg
        left_fab.requestFocus()

        if (!isStopRequired) {
            var fadeIn = AlphaAnimation(0.0f, 1.0f)
            var fadeOut = AlphaAnimation(1.0f, 0.0f)
            tv_message.startAnimation(fadeIn)
            tv_message.startAnimation(fadeOut)
            fadeIn.duration = 1200
            fadeIn.fillAfter = true
            fadeOut.duration = 1200
            fadeOut.fillAfter = true
            fadeOut.startOffset = calculateFadeInOut(msg) + fadeIn.startOffset
        }
    }

    fun calculateFadeInOut(msgToShow: String): Long {
        return ((msgToShow.length * 100) + 300).toLong()
    }
}


