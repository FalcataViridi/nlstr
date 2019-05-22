package vocs.nlstr.interfaces

import android.os.Bundle
import vocs.nlstr.servicios.RecognitionStatus
import java.util.ArrayList

/**
 * Created by Moises on 29/07/2018.
 */
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
    fun onKeywordDetected(keys: ArrayList<String>) { }

}