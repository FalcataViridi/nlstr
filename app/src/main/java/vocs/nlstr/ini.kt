package vocs.nlstr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener

class ini : AppCompatActivity(),RecognitionListener {


    //VARIABLES
    //Variables gráficas


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ini)
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRmsChanged(p0: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBufferReceived(p0: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPartialResults(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeginningOfSpeech() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onEndOfSpeech() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResults(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
