package vocs.nlstr.views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import vocs.nlstr.R
import vocs.nlstr.utils.inTransaction
import java.util.ArrayList

class HomeActivity : AppCompatActivity() {

    var isListening: Boolean = true
    var isCommand: Boolean = false
    var matches = ArrayList<String>()
    var DEFAULT_HEIGHT = 30


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager.inTransaction { add(R.id.fragContainerHome, HomeFragment()) }

        configListeners()
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

    private fun initView() {
        matches.add("bravo")
    }

    fun getCurrentFragment(): Fragment {
        return supportFragmentManager.findFragmentById(R.id.fragContainerHome)
    }

    fun startReconAnimation() {
        waveHeader.run {  }
    }

    fun stopReconAnimation() {
        waveHeader.stop()
    }

    fun configWavesHeight (height: Int) {
        waveHeader.waveHeight += 1

    }
}


