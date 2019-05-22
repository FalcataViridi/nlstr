package vocs.nlstr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.servicios.RecognitionManager
import vocs.nlstr.servicios.RecognitionStatus

class Ini : AppCompatActivity(), RecognitionCallback {

    //VARIABLES
    //componentes gráficos
    @BindView(R.id.txtMulSpeech)
    lateinit var txtMulSpeech: TextView

    @BindView(R.id.barPrgSpeech)
    lateinit var barPrgSpeech: ProgressBar

    @BindView(R.id.btnTglSpeech)
    lateinit var btnTglSpeech: ToggleButton

    lateinit var reconManager: RecognitionManager

    var isCommand: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ini)

        ButterKnife.bind(this)

        btnTglSpeech.visibility = View.INVISIBLE
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.max = 10

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }

        var matches = ArrayList<String>()
        matches.add("bravo")
        reconManager = RecognitionManager(this, matches, this, isCommand)
    }

    //------------METODOS HEREDADOS---------------------//
    override fun onPrepared(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.SUCCESS -> {
                btnTglSpeech.visibility = View.VISIBLE
                btnTglSpeech.setOnCheckedChangeListener { _, isChecked ->

                    //Determinamos si hay reconocimiento segun estado de boton
                    if (isChecked) {
                        startRecognition()
                        txtMulSpeech.text = "Recognition ready"
                    } else {
                        stopRecognition()
                    }
                }
            }

            RecognitionStatus.FAILURE, RecognitionStatus.UNAVAILABLE -> {
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

    /*override fun onKeywordDetected(keys: ArrayList<String>) {
        txtMulSpeech.text = "Keyword Detected"
    }*/

    override fun onReadyForSpeech(params: Bundle) {
        Log.i("Recognition","onReadyForSpeech")
    }

    override fun onBufferReceived(buffer: ByteArray) {
        Log.i("Recognition","onBufferReceived")
    }

    //Usaremos el Progress Bar como indicador de dB de voz
    override fun onRmsChanged(rmsdB: Float) {
        barPrgSpeech.progress = rmsdB.toInt()
    }

    override fun onPartialResults(results: List<String>) {
        val text = results.joinToString(separator = "\n")
        Log.i("Recognition","onPartialResult")
    }

    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = results.joinToString(separator = "\n")
        Log.i("Recognition","onResult")
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Log.i("Recognition","onEvent")
    }

    override fun onBeginningOfSpeech() {
        barPrgSpeech.isIndeterminate = false
        Log.i("Recognition","onBeginningOfSpeech")
    }

    override fun onEndOfSpeech() {
        Log.i("Recognition","onEndOfSpeech")
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.i("Recognition","onEndOfSpeech - $errorCode")
        //txtMulSpeech.text = errorMessage
        btnTglSpeech.isChecked = false
    }

    //METODOS NO HEREDADOS//
    private fun startRecognition() {
        Log.i("Recognition","startRecognition")
        btnTglSpeech.isChecked = true
        barPrgSpeech.visibility = View.VISIBLE
        reconManager.startRecognition()
    }

    private fun stopRecognition() {
        Log.i("Recognition","stopRecognition")
        btnTglSpeech.isChecked = false
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.isIndeterminate = true
        reconManager.stopRecognition()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecognition()
                }
            }
        }
    }
}






