package vocs.nlstr.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import vocs.nlstr.R
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.servicios.RecognitionManager
import vocs.nlstr.utils.RecognitionStatus
import vocs.nlstr.utils.TranslationKeys
import java.util.*
import android.os.AsyncTask.execute
import android.R



class HomeFragment : Fragment(), RecognitionCallback {

    var reconManager: RecognitionManager? = null

    var acceptedTextToTranslate: String = ""


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
        if (!reconManager?.isActive!!) startRecognition()
    }

    override fun onDestroy() {
        reconManager?.destroyRecognizer()
        super.onDestroy()
    }

    override fun onPrepared(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.SUCCESS -> {
                if ((activity as HomeActivity).isListening) {
                    startRecognition()
                } else stopRecognition()
            }

            RecognitionStatus.FAILURE, RecognitionStatus.UNAVAILABLE -> {
                //TODO: define in failure case
            }
        }
    }

    override fun onKeywordDetected(key: String) {
        super.onKeywordDetected(key)
        activateFunction(key)
    }

    override fun onBeginningOfSpeech() {
        Log.i("Recognition", "onBeginningOfSpeech")
    }

    override fun onReadyForSpeech(params: Bundle) {
        Log.i("Recognition", "onReadyForSpeech")
        reconManager!!.keyWords = getKeywords()
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
        val text = results[0]
        Log.i("Recognition", "onResult - $text")


        var textResult = results.joinToString { "$it " }
        var previousText = acceptedTextToTranslate.plus(" ").plus(textResult)
        var spannedText = SpannableString(previousText)

        spannedText.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorAccent))
                , acceptedTextToTranslate.length
                , previousText.length
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        et_translate.setText(textResult)
        tv_translate_accepted.text = spannedText
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

    //--------------------- NO HEREDADO ---------------//
    private fun configPermission() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    private fun initView() {
        et_translate.hint = TranslationKeys.TRADUCIR.name
        tv_translated.hint = TranslationKeys.TRADUCIDO.name
        reconManager = RecognitionManager(context!!, this, (activity as HomeActivity).isCommand)
    }

    fun startRecognition() {
        Log.i("Recognition", "startRecognition")
        reconManager?.startRecognition()
        (activity as HomeActivity).startReconAnimation()
    }

    fun stopRecognition() {
        Log.i("Recognition", "stopRecognition")
        reconManager?.stopRecognition()
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

    private fun getKeywords(): ArrayList<String> {
        var matches = ArrayList<String>()
        matches.addAll((activity as HomeActivity).matches)
        matches.addAll(getTranslationKeys())

        return matches
    }

    fun getTranslationKeys(): Collection<String> {
        var matches = ArrayList<String>()
        matches.add(TranslationKeys.TRADUCIR.name)
        matches.add(TranslationKeys.TRADUCIDO.name)
        matches.add(TranslationKeys.ACEPTAR.name)
        matches.add(TranslationKeys.BORRAR.name)

        return matches
    }


    fun activateFunction(key: String) {
        when (key) {
            TranslationKeys.TRADUCIR.name -> translateAction()
            TranslationKeys.ACEPTAR.name -> acceptAction()

            TranslationKeys.TRADUCIDO.name -> tv_translated.requestFocus()

            TranslationKeys.BORRAR.name -> deleteAction()
        }
    }

    private fun deleteAction() {
        tv_translate_accepted.text = ""
        acceptedTextToTranslate = ""
    }

    private fun translateAction() {
        tv_translate_accepted.requestFocus()

        val googleTranslate = GoogleTranslate()
// Perform the translation by invoking the execute method, but first save the result in a String.
// The second parameter is the source language, the third is the terget language
        val result = googleTranslate.execute("the text to be translated", "en", "de").get()

        //TODO: service call to translation
    }

    private fun acceptAction() {
        acceptedTextToTranslate = acceptedTextToTranslate.plus(et_translate.text)
        tv_translate_accepted.text = acceptedTextToTranslate
    }
}






