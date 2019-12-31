package vocs.nlstr.views.Lists

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list_shopping.*
import vocs.nlstr.R
import vocs.nlstr.adapters.ShoppingListAdapter
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.models.ShoppingListItemData
import vocs.nlstr.servicios.RecognitionManager
import vocs.nlstr.utils.MainListItemAttributes
import vocs.nlstr.utils.MainListKeys
import vocs.nlstr.utils.RecognitionStatus
import vocs.nlstr.utils.ShoppingListItemAttributes
import vocs.nlstr.views.HomeActivity
import java.util.*


class ShoppingListFragment : Fragment(), RecognitionCallback {
    lateinit var adapter: ShoppingListAdapter

    var elementChanging = ""
    var listOfLists = ArrayList<ShoppingListItemData>()
    var textResult: String = ""
    var reconManager: RecognitionManager? = null
    var isCreatingNew: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_shopping, container, false)
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
                if ((activity as HomeActivity).isListening) startRecognition()
                else stopRecognition()
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

        textResult = results.joinToString { "$it " }

        updatingItem(textResult)
    }

    fun updatingItem(text: String) {
        when (elementChanging) {
            MainListItemAttributes.TITULO.name -> adapter.updateingInfo(
                    0
                    , text)
            MainListItemAttributes.DESCRIPCION.name -> adapter.updateingInfo(
                    0
                    , text)
        }
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
        reconManager = RecognitionManager(context!!, this)
        listOfLists = getLists()
        rv_shopping_list.layoutManager = LinearLayoutManager(context!!)

        adapter = ShoppingListAdapter(listOfLists, context!!)
        rv_shopping_list.adapter = adapter
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
        matches.addAll(getMainListKeys())

        return matches
    }

    fun getMainListKeys(): Collection<String> {
        var matches = ArrayList<String>()
        matches.add(MainListKeys.ACEPTAR.key)
        matches.add(MainListKeys.BORRAR.key)
        matches.add(MainListKeys.CREAR.key)
        matches.add(MainListKeys.ACEPTAR.key)

        return matches
    }

    fun activateFunction(key: String) {
        when (key) {
            MainListKeys.CREAR.key -> createItemAction()
            MainListKeys.ACEPTAR.key -> acceptAction()
        }
    }

    private fun acceptAction() {
        elementChanging = when (elementChanging) {
            ShoppingListItemAttributes.TITULO.name -> ShoppingListItemAttributes.DESCRIPCION.name
            ShoppingListItemAttributes.DESCRIPCION.name -> ShoppingListItemAttributes.STATUS.name

            else -> MainListItemAttributes.TITULO.name
        }

        adapter.activateElement(0, elementChanging)
    }

    private fun createItemAction() {
        //Si hay creandose alguna lista se borrará
        if (isCreatingNew) adapter.removeByItem(listOfLists[0])
        else isCreatingNew = true

        adapter.deactivateView(0)
        elementChanging = MainListItemAttributes.TITULO.name

        var data = ShoppingListItemData(Date().time, "", "", "")
        adapter.insert(0, data, elementChanging)

    }

    fun getLists(): ArrayList<ShoppingListItemData> {
        var lists = ArrayList<ShoppingListItemData>()
        lists.add(ShoppingListItemData(Date().time, "Huevos", "una dozena", "listado de compra"))
        lists.add(ShoppingListItemData(Date().time, "Pan", "2 barras", "List TODO"))
        lists.add(ShoppingListItemData(Date().time, "Aceite Girasol", "Garrafa", "Lista Tareas"))
        lists.add(ShoppingListItemData(Date().time, "Leche de coco", "1 tetrabrik", "Historial"))
        return lists
    }
}