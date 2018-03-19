package vocs.nlstr

import android.content.Intent
import vocs.nlstr.servicios.RecognitionManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import timber.log.Timber
import java.io.File.separator

class ini : AppCompatActivity(), RecognitionManager.RecognitionCallback{

    //VARIABLES
    //componentes gráficos
    @BindView(R.id.txtMulSpeech)
    lateinit var txtMulSpeech: TextView

    @BindView(R.id.barPrgSpeech)
    lateinit var barPrgSpeech: ProgressBar

    @BindView(R.id.btnTglSpeech)
    lateinit var btnTglSpeech: ToggleButton

    lateinit var reconManager: RecognitionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ini)

        ButterKnife.bind(this)

        btnTglSpeech.visibility = View.INVISIBLE
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.max = 10

        reconManager = RecognitionManager(this, "BRAVO", buildRecognizerIntent(), this)
    }



    //------------METODOS HEREDADOS---------------------//
    override fun onPrepared(status: RecognitionManager.RecognitionStatus)
    {
        when (status) {
            RecognitionManager.RecognitionStatus.SUCCESS -> {
                btnTglSpeech.visibility = View.VISIBLE
                btnTglSpeech.setOnCheckedChangeListener { _, isChecked ->

                    //Determinamos si hay reconocimiento segun estado de boton
                    if (isChecked) {startRecognition()
                        txtMulSpeech.text = "Recognition ready"
                    } else {stopRecognition()}
                }
            }

            RecognitionManager.RecognitionStatus.FAILURE
            ,RecognitionManager.RecognitionStatus.UNAVAILABLE -> {
                MaterialDialog.Builder(this)
                        .title("No disponible")
                        .content("Error en su servicio de reconocimiento")
                        .positiveText(android.R.string.ok)
                        .show()

                txtMulSpeech.text = "Recognition unavailable"
            }
        }

    }

    override fun onDestroy() {
        reconManager.destroyRecognizer()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        reconManager.startRecognition()
    }

    override fun onKeywordDetected() {
        txtMulSpeech.text = "Keyword Detected"
    }

    override fun onReadyForSpeech(params: Bundle)
    {
        Timber.i(this.localClassName +" - onReadyForSpeech")
    }

    override fun onBufferReceived(buffer: ByteArray)
    {
        Timber.i(this.localClassName +" - onPartialResults")
    }

    override fun onRmsChanged(rmsdB: Float)
    {
        //Usaremos el Progress Bar como indicador de dB de voz
        barPrgSpeech.progress = rmsdB.toInt()
    }

    override fun onPartialResults(results: List<String>) {
        val text = results.joinToString(separator="\n")
        Timber.i("onResults : %s", text)
    }


    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = results.joinToString(separator="\n")
        Timber.i("onResults : %s", text, scores.toString())
    }

    override fun onEvent(eventType: Int, params: Bundle)
    {
        Timber.i(this.localClassName +" - onEvent")
    }

    override fun onBeginningOfSpeech()
    {
        barPrgSpeech.isIndeterminate = false
        Timber.i(this.localClassName +" - onBeginningOfSpeech")
    }

    override fun onEndOfSpeech()
    {
        Timber.i(this.localClassName +" - onEndOfSpeech")
    }

    override fun onError(errorCode: Int)
    {
        val errorMessage = getErrorText(errorCode)
        Timber.i("FAILED %s", errorMessage)
        txtMulSpeech.text = errorMessage
        btnTglSpeech.isChecked = false
    }


    //METODOS NO HEREDADOS//
    private fun startRecognition()
    {
        Timber.i(this.localClassName +" - startRecognition")
        btnTglSpeech.isChecked = true
        barPrgSpeech.visibility = View.VISIBLE
        reconManager.startRecognition()

    }

    private fun stopRecognition()
    {
        Timber.i(this.localClassName +" - startRecognition")
        btnTglSpeech.isChecked = false
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.isIndeterminate = true
        reconManager.stopRecognition()
    }

    private fun buildRecognizerIntent(): Intent
    {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        return recognizerIntent
    }

    private fun getErrorText(errorCode: Int): String
    {
        when (errorCode)
        {
            SpeechRecognizer.ERROR_AUDIO -> return  "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> return  "Error conexion con cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> return  "No tengo permisos"
            SpeechRecognizer.ERROR_NETWORK -> return  "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> return  "Error de tiempo"
            SpeechRecognizer.ERROR_NO_MATCH -> return  "Sin coicidencias"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> return "Recon saturado"
            SpeechRecognizer.ERROR_SERVER -> return  "Error desde servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> return  "No se detecta Input"
            else -> return  "Error generico"
        }
    }
}






