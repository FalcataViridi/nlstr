package vocs.nlstr.servicios

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import timber.log.Timber
import vocs.nlstr.interfaces.RecognitionCallback

/**
 * Created by Moises on 04/01/2018.
 */

class RecognitionManager(private val context: Context
                         , private val keyWords: ArrayList<String>
                         , private val callback: RecognitionCallback? = null
                         , private val isCommand: Boolean = false
) : RecognitionListener {

    var isActive: Boolean = false
    var isListening: Boolean = false
    var shouldMute: Boolean = false
    var recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var speechRecog: SpeechRecognizer? = null


    init {
        setRecognizerPreferences()
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecog?.setRecognitionListener(this)

            callback?.onPrepared(
                    if (null != speechRecog) RecognitionStatus.SUCCESS
                    else RecognitionStatus.FAILURE
            )
        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    fun startRecognition() {
        if (!isListening) {
            isListening = true
            speechRecog?.startListening(recognizerIntent)
        }

    }

    fun cancelRecognition() {
        Toast.makeText(context, "Cancel recognition", Toast.LENGTH_SHORT).show()
        //Timber.i(this.toString() + " - cancelRecognition")
        speechRecog?.cancel()
    }

    fun stopRecognition() {
        speechRecog?.stopListening()
    }

    fun destroyRecognizer() {
        muteRecognition(false)
        speechRecog?.destroy()
    }

    override fun onReadyForSpeech(params: Bundle) {
        muteRecognition(shouldMute || !isActive)
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
        isActive = false
        isListening = false

        //Si no reacciona volvemos a comenzar, eliminamos el recog y lo reiniciamos
        when (errorCode) {
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> cancelRecognition()
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                destroyRecognizer()
                initializeRecognizer()
            }

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                destroyRecognizer()
                initializeRecognizer()
            }

            else -> {
            }
        }

        //TODO("Definir como indicar el error")
        //FIXME: Temporalmente si existe un error se inicia el reconocimiento

        startRecognition()
    }


    override fun onResults(results: Bundle) {

        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        if (null != matches) {
            if (isActive) {
                isActive = false
                callback?.onResults(matches, scores)
            } else {
                if (isCommand) {
                    if (keyMatcher(matches[0]).size > 0) callback?.onKeywordDetected(keyMatcher(matches[0]))
                } else {
                    if (keyMatcher(matches[0]).size > 0) callback?.onKeywordDetected(matches)

                }
            }
        }
        isListening = false
        startRecognition()
    }

    fun keyMatcher(textToMatch: String): ArrayList<String> {
        var matches = ArrayList<String>()

        keyWords.forEach {
            if (textToMatch.toLowerCase().contains(it)) matches.add(it)
        }

        return matches
    }

    override fun onPartialResults(results: Bundle) {
        val matcheS = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

        if (isActive && null != matcheS) callback?.onPartialResults(matcheS)
    }

    private fun muteRecognition(isMuted: Boolean) {
        val flag = if (isMuted) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0)
    }

    private fun setRecognizerPreferences() {
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }
}
