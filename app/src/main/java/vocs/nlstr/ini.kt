package vocs.nlstr

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
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

class ini : AppCompatActivity(),RecognitionListener {


    //VARIABLES
    //componentes gráficos
    @BindView(R.id.txtMulSpeech)
    lateinit var txtMulSpeech: TextView

    @BindView(R.id.barPrgSpeech)
    lateinit var barPrgSpeech: ProgressBar

    @BindView(R.id.btnTglSpeech)
    lateinit var btnTglSpeech: ToggleButton

    private var speechRecog: SpeechRecognizer? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ini)

        ButterKnife.bind(this)

        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.max = 10

        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecog?.setRecognitionListener(this)

            //Definimos metodos segun stado del boton
            btnTglSpeech.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) startRecognition()
                else stopRecognition()
            }
        } else MaterialDialog.Builder(this)
                .title("RECON NO DISPONIBLE")
                .content("RECON NO SOPORTADO!!")
                .positiveText(android.R.string.ok)
                .show()
    }

    //------------METODOS---------------------//
    //METODOS HEREDADOS A SPEECH RECOGNIZER
    override fun onReadyForSpeech(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRmsChanged(p0: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBufferReceived(p0: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPartialResults(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeginningOfSpeech() {
        barPrgSpeech.isIndeterminate = false

        Timber.i(this.localClassName +" - onBeginningOfSpeech")
    }

    override fun onEndOfSpeech() {
        barPrgSpeech.isIndeterminate = true

        Timber.i(this.localClassName +" - onEndOfSpeech")
    }

    override fun onError(errorCode: Int) {

        val errorMessage = getErrorText(errorCode)
        Timber.d("FAILED %s", errorMessage)
        txtMulSpeech.text = errorMessage
        btnTglSpeech.isChecked = false
    }

    override fun onResults(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //METODOS NO HEREDADOS//
    private fun startRecognition() {
        btnTglSpeech.isChecked = true
        barPrgSpeech.visibility = View.VISIBLE
        barPrgSpeech.isIndeterminate= true
        speechRecog?.startListening(buildRecognizerIntent())
    }

    private fun stopRecognition() {
        btnTglSpeech.isChecked = false
        barPrgSpeech.visibility = View.INVISIBLE
        barPrgSpeech.isIndeterminate = false
        speechRecog?.stopListening()
    }

    private fun buildRecognizerIntent(): Intent {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        return recognizerIntent
    }

    private fun getErrorText(errorCode: Int): String {
        when (errorCode) {
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






