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
import android.widget.Toast
import android.widget.ToggleButton
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.servicios.RecognitionStatus

class Ini : AppCompatActivity(), RecognitionCallback{


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

        reconManager = RecognitionManager(this, "bravo", this)
    }



    //------------METODOS HEREDADOS---------------------//
    override fun onPrepared(status: RecognitionStatus)
    {
        when (status) {
            RecognitionStatus.SUCCESS -> {
                btnTglSpeech.visibility = View.VISIBLE
                btnTglSpeech.setOnCheckedChangeListener { _, isChecked ->

                    //Determinamos si hay reconocimiento segun estado de boton
                    if (isChecked) {startRecognition()
                        txtMulSpeech.text = "Recognition ready"
                    } else {stopRecognition()}
                }
            }

            RecognitionStatus.FAILURE
            ,RecognitionStatus.UNAVAILABLE -> {
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
        startRecognition()
    }

    override fun onKeywordDetected(keyWord: String) {
        txtMulSpeech.text = "Keyword Detected"
    }

    override fun onReadyForSpeech(params: Bundle)
    {
       Toast.makeText(this, "onReadyForSpeech", Toast.LENGTH_LONG)
    }

    override fun onBufferReceived(buffer: ByteArray)
    {
        Toast.makeText(this, "onReadyForSpeech", Toast.LENGTH_LONG)
    }

    override fun onRmsChanged(rmsdB: Float)
    {
        //Usaremos el Progress Bar como indicador de dB de voz
        barPrgSpeech.progress = rmsdB.toInt()
    }

    override fun onPartialResults(results: List<String>) {
        val text = results.joinToString(separator="\n")
        Toast.makeText(this, "onResults : %s", Toast.LENGTH_LONG)
    }


    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = results.joinToString(separator="\n")
        Toast.makeText(this, "onResults : %s", Toast.LENGTH_LONG)
    }

    override fun onEvent(eventType: Int, params: Bundle)
    {
        Toast.makeText(this, "onEvent", Toast.LENGTH_LONG)
    }

    override fun onBeginningOfSpeech()
    {
        barPrgSpeech.isIndeterminate = false
        Toast.makeText(this, "onBeginning", Toast.LENGTH_LONG)
    }

    override fun onEndOfSpeech()
    {
        Toast.makeText(this, "onEnding : %s", Toast.LENGTH_LONG)
    }

    override fun onError(errorCode: Int)
    {
        val errorMessage = getErrorText(errorCode)
        Toast.makeText(this, "onError - ${getErrorText(errorCode)}", Toast.LENGTH_LONG)
        txtMulSpeech.text = errorMessage
        btnTglSpeech.isChecked = false
    }


    //METODOS NO HEREDADOS//
    private fun startRecognition()
    {
        Toast.makeText(this, "onStart", Toast.LENGTH_LONG)
        btnTglSpeech.isChecked = true
        barPrgSpeech.visibility = View.VISIBLE
        reconManager.startRecognition()

    }

    private fun stopRecognition()
    {
        Toast.makeText(this, "onStop", Toast.LENGTH_LONG)
        btnTglSpeech.isChecked = false
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.isIndeterminate = true
        reconManager.stopRecognition()
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






