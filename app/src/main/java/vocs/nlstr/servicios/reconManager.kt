package vocs.nlstr.servicios

import android.Manifest
import android.R
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Toast
import butterknife.ButterKnife
import timber.log.Timber
import android.content.pm.PackageManager
import android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission
import android.Manifest.permission
import android.Manifest.permission.RECORD_AUDIO
import android.os.Build
import android.support.v13.app.FragmentCompat.requestPermissions
import android.support.v4.content.ContextCompat.startActivity
import android.content.ComponentName




/**
 * Created by Moises on 04/01/2018.
 */

class RecognitionManager(private val context: Context
                         ,private val activationWord: String
                         ,private val recognizerIntent: Intent
                         ,private val callback: RecognitionCallback? = null
                        ): RecognitionListener
{
    private var speechRecog: SpeechRecognizer? = null

    var isActive: Boolean = false

    init
    {
        Timber.i(this.toString() + " - INICIO")
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecog?.setRecognitionListener(this)

            //Cuando creamos el callback nos aseguramos de setear el estado del speech recognizer
            callback?.onPrepared(
                    if (null != speechRecog) RecognitionStatus.SUCCESS else RecognitionStatus.FAILURE
            )

        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    fun startRecognition()
    {
        Timber.i(this.toString() + " - startRecognition")
        cancelRecognition()
        speechRecog?.startListening(recognizerIntent)
    }

    fun cancelRecognition() {
        Timber.i(this.toString() + " - cancelRecognition")
        speechRecog?.cancel()
    }

    fun stopRecognition() {
        speechRecog?.stopListening()
    }

    fun destroyRecognizer(){
        speechRecog?.destroy()
    }

    override fun onReadyForSpeech(params: Bundle) {
        callback?.onReadyForSpeech(params)
    }

    override fun onRmsChanged(rmsdB: Float) {
        callback?.onRmsChanged(rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        callback?.onBufferReceived(buffer)
    }

    override fun onEvent(EventType: Int, params: Bundle) {
        Timber.i(this.toString() + " Evento: $EventType")
        callback?.onEvent(EventType, params)
    }

    override fun onBeginningOfSpeech() {
        callback?.onBeginningOfSpeech()
    }

    override fun onEndOfSpeech() {
        callback?.onEndOfSpeech()
    }

    override fun onError(errorCode: Int) {

        Timber.e(this.toString() + " - reconManager.onError - $errorCode")

        //Si esta activado definiremos que es un error
        if (isActive) {
            callback?.onError(errorCode)
        }

        //Si no reacciona volvemos a comenzar, eliminamos el recog y lo reiniciamos
        when (errorCode) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {

                //TODO: crear si es necesario un control por funcion de conocer permiso y peticion si falta

                destroyRecognizer()
                initializeRecognizer()
            }

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                destroyRecognizer()
                initializeRecognizer()
            }

            else -> {}
        }

        //TODO("Definir como indicar el error")
        //FIXME: Temporalmente si existe un error se inicia el reconocimiento

        startRecognition()
    }


    override fun onResults(results: Bundle) {

        //FIXME: arreglar fallo en recognizer error 6
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores =  results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        if (null != matches) {
            if (isActive){
                isActive = false
                callback?.onResults(matches, scores)
            }else {
                matches.forEach{
                    if (it.contains(other = activationWord, ignoreCase = true)){
                        isActive = true
                        callback?.onKeywordDetected()
                        return@forEach
                    }
                }
            }

        }

        startRecognition()
    }

    override fun onPartialResults(results: Bundle) {
        val matcheS = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

        if (null != matcheS) callback?.onPartialResults(matcheS)
    }

    //Interfaz de los metodos heredados
    interface RecognitionCallback {
        fun onPrepared(status: RecognitionStatus)
        fun onBeginningOfSpeech()
        fun onReadyForSpeech(params: Bundle)
        fun onBufferReceived(buffer: ByteArray)
        fun onRmsChanged(rmsdB: Float)
        fun onPartialResults(results: List<String>)
        fun onResults(results: List<String>, scores: FloatArray?)
        fun onError(errorCode: Int)
        fun onEvent(eventType: Int, params: Bundle)
        fun onEndOfSpeech()
        fun onKeywordDetected() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    /**
     * Posibles estados del recon
     */
    enum class RecognitionStatus {
        SUCCESS, FAILURE, UNAVAILABLE
    }
}
