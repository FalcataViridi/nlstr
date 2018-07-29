package vocs.nlstr.servicios

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.media.AudioManager
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Toast
import timber.log.Timber

/**
 * Created by Moises on 04/01/2018.
 */

class RecognitionManager(private val context: Context
                         ,private val wordsComm: List<String>?
                         ,private val recognizerIntent: Intent
                         ,private val callback: RecognitionCallback? = null
                        ): RecognitionListener
{

    var isActive: Boolean = false

    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var speechRecog: SpeechRecognizer? = null
    private var commandsOnlyRecognition: List<String>? = wordsComm


    init
    {
        Toast.makeText(context, "inicio", Toast.LENGTH_SHORT).show()
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecog?.setRecognitionListener(this)

            //Cuando creamos el callback nos aseguramos de setear el estado del speech recognizer
            callback?.onPrepared(
                    if (null != speechRecog) RecognitionStatus.SUCCESS
                    else RecognitionStatus.FAILURE
            )
        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    fun startRecognition()
    {
        Toast.makeText(context, "start recognition", Toast.LENGTH_SHORT).show()
        cancelRecognition()
        speechRecog?.startListening(recognizerIntent)
    }

    fun cancelRecognition() {
        var toastMsg: Toast
        Toast.makeText(context, "Cancel recognition", Toast.LENGTH_SHORT).show()
        //Timber.i(this.toString() + " - cancelRecognition")
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

        print(scores);

        if (null != matches) {
            if (isActive){
                isActive = false
                callback?.onResults(matches, scores)
            }else {
                matches.forEach{
                    if (it.contains(other = "candy", ignoreCase = true)){
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
