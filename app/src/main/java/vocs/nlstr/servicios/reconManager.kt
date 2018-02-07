package vocs.nlstr.servicios

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

/**
 * Created by Moises on 04/01/2018.
 */

class RecognitionManager(context: Context
                         , private val recognizerIntent: Intent
                         ,private val callback: RecognitionCallback? = null
                        ): RecognitionListener
{
    private var speechRecog: SpeechRecognizer? = null

    init
    {
        if (SpeechRecognizer.isRecognitionAvailable(context))
        {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecog?.setRecognitionListener(this)

            //Cuando creamos el callback nos aseguramos de setear el estado del speech recognizer
            callback?.onPrepared(
                    if (null != speechRecog) RecognitionStatus.SUCCESS else RecognitionStatus.FAILURE
            )

        } else
        {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }
    fun startRecognition()
    {
        speechRecog?.cancel()
        speechRecog?.startListening(recognizerIntent)
    }

    fun stopRecognition()
    {
        speechRecog?.stopListening()
    }

    override fun onReadyForSpeech(params: Bundle)
    {
        callback?.onReadyForSpeech(params)
    }

    override fun onRmsChanged(rmsdB: Float)
    {
        callback?.onRmsChanged(rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray)
    {
        callback?.onBufferReceived(buffer)
    }

    override fun onEvent(EventType: Int, params: Bundle)
    {
        callback?.onEvent(EventType, params)
    }

    override fun onBeginningOfSpeech()
    {
        callback?.onBeginningOfSpeech()
    }

    override fun onEndOfSpeech()
    {
        callback?.onEndOfSpeech()
    }

    override fun onError(errorCode: Int)
    {
        callback?.onError(errorCode)

        TODO("Definir como indicar el error")
        //FIXME: Temporalmente si existe un error se inicia el reconocimiento

        startRecognition()
    }


    override fun onResults(results: Bundle)
    {
        TODO("falta decidir nuevas implementaciones como estadisticas")

        val matcheS = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scoreS =  results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        if (null != matcheS) callback?.onResults(matcheS, scoreS)

        startRecognition()

    }

    override fun onPartialResults(results: Bundle)
    {
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
    }

    /**
     * Posibles estados del recon
     */
    enum class RecognitionStatus {
        SUCCESS, FAILURE, UNAVAILABLE
    }
}
