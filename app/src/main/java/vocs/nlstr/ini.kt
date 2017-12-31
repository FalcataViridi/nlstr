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
                .content("RECON NO SOPORTADO")
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onEndOfSpeech() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

}






