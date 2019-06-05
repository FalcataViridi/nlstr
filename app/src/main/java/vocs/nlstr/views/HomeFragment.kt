package vocs.nlstr.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import vocs.nlstr.R
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.servicios.RecognitionManager
import vocs.nlstr.utils.RecognitionStatus
import java.util.*


class HomeFragment : Fragment(), RecognitionCallback {

    var reconManager: RecognitionManager? = null
    var isResultDone: Boolean = true
    var hasStarted: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configPermission()
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (!reconManager?.isListening!!) startRecognition()
    }

    override fun onDestroy() {
        reconManager?.destroyRecognizer()
        super.onDestroy()
    }

    override fun onPrepared(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.SUCCESS -> {
                if ((activity as HomeActivity).isListening) {
                    hasStarted = true
                    startRecognition()
                } else stopRecognition()
            }

            RecognitionStatus.FAILURE, RecognitionStatus.UNAVAILABLE -> {
                //TODO: define in failure case
            }
        }
    }

    override fun onKeywordDetected(keys: ArrayList<String>) {
        super.onKeywordDetected(keys)
    }

    override fun onBeginningOfSpeech() {
        Log.i("Recognition", "onBeginningOfSpeech")
    }

    override fun onReadyForSpeech(params: Bundle) {
        Log.i("Recognition", "onReadyForSpeech")
    }

    override fun onBufferReceived(buffer: ByteArray) {
        Log.i("Recognition", "onBufferReceived")
    }

    override fun onRmsChanged(rmsdB: Float) {
        //(activity as HomeActivity).configWavesHeight(rmsdB.toInt())
    }

    override fun onPartialResults(results: List<String>) {
        val text = results[0] + "..." + results[results.size - 1]
        Log.i("Recognition", "onPartialResult - $text")
    }

    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = results[0] + "..." + results[results.size - 1]
        Log.i("Recognition", "onResult - $text")
        var wholeText = results.joinToString { "$it " }
        et_up.setText(wholeText)
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.i("Recognition", "onEndOfSpeech - $errorMessage")
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Log.i("Recognition", "onEvent")
    }

    override fun onEndOfSpeech() {
        Log.i("Recognition", "onEndOfSpeech")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecognition()
                }
            }
        }
    }

    //---------------------NO HEREDADO ----------//
    private fun configPermission() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    private fun initView() {
        reconManager = RecognitionManager(context!!, (activity as HomeActivity).matches, this, (activity as HomeActivity).isCommand)
    }

    fun startRecognition() {
        Log.i("Recognition", "startRecognition")
        reconManager?.startRecognition()
        (activity as HomeActivity).startReconAnimation()
    }

    fun stopRecognition() {
        Log.i("Recognition", "stopRecognition")
        reconManager?.stopRecognition()
        hasStarted = false
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> "Error conexion con cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "No tengo permisos"
            SpeechRecognizer.ERROR_NETWORK -> "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de tiempo"
            SpeechRecognizer.ERROR_NO_MATCH -> "Sin coicidencias"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recon saturado"
            SpeechRecognizer.ERROR_SERVER -> "Error desde servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detecta Input"
            else -> "Error generico"
        }
    }
}





