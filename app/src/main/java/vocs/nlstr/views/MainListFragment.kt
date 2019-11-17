package vocs.nlstr.views

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
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_list_main.*
import vocs.nlstr.R
import vocs.nlstr.adapters.MainListAdapter
import vocs.nlstr.interfaces.RecognitionCallback
import vocs.nlstr.models.MainListItemData
import vocs.nlstr.servicios.RecognitionManager
import vocs.nlstr.utils.MainListItemAttributes
import vocs.nlstr.utils.MainListKeys
import vocs.nlstr.utils.RecognitionStatus
import java.util.*
import kotlin.collections.ArrayList


class MainListFragment : Fragment(), RecognitionCallback {

    lateinit var adapter: MainListAdapter

    var elementChanging = ""
    var listOfLists = ArrayList<MainListItemData>()
    var listOfListsSelected = ArrayList<MainListItemData>()
    var textResult: String = ""
    var reconManager: RecognitionManager? = null
    var isCreatingNew = false
    var isSelecting = false
    var selectedItems = ArrayList<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_main, container, false)
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

        if (isSelecting) {
            listOfListsSelected.clear()
            var index = 0
            listOfLists.forEach {
                if (it.name.toLowerCase().contains(textResult.trim())) {
                    listOfListsSelected.add(listOfLists[index])
                }
                index++
            }
            adapter.selectItems(listOfListsSelected)
        } else if (isCreatingNew) updatingItem(textResult)
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
        reconManager = RecognitionManager(context!!, this, (activity as HomeActivity).isCommand)
        listOfLists = getLists()
        rv_main_list.layoutManager = LinearLayoutManager(context!!)

        adapter = MainListAdapter(listOfLists, context!!)
        rv_main_list.adapter = adapter
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
        matches.add(MainListKeys.SIGUIENTE.key)
        matches.add(MainListKeys.BORRAR.key)
        matches.add(MainListKeys.CREAR.key)
        matches.add(MainListKeys.ACEPTAR.key)
        matches.add(MainListKeys.SELECCIONAR.key)
        matches.add(MainListKeys.COMANDOS.key)

        return matches
    }

    fun activateFunction(key: String) {
        when (key) {
            MainListKeys.CREAR.key -> createItemAction()
            MainListKeys.SIGUIENTE.key -> nextAction()
            MainListKeys.ACEPTAR.key -> acceptAction()
            MainListKeys.SELECCIONAR.key -> selectAction()
            MainListKeys.BORRAR.key -> deleteItemAction(listOfListsSelected)
            MainListKeys.COMANDOS.key -> {
                var commandList = ArrayList<String>()
                MainListKeys.values().forEach {
                    commandList.add(it.key)
                }

                (activity as HomeActivity).showCommands(commandList)
            }
        }
    }

    private fun selectAction() {
        (activity as HomeActivity).showMessage("Seleccionando")

        addAllListToSelected(listOfLists)

        adapter.selectItems(listOfLists)

        isSelecting = true
        isCreatingNew = false
    }

    private fun addAllListToSelected(listComplete: ArrayList<MainListItemData>) {
        var index = 0
        listComplete.forEach {
            selectedItems.add(index++)
        }
    }

    private fun acceptAction() {
        (activity as HomeActivity).showMessage("Seleccionado?")

        isCreatingNew = false
        if (isSelecting) {
            if (selectedItems.size > 1) Toast.makeText(activity, "Seleccione una sola lista", Toast.LENGTH_LONG).show()
            else if (selectedItems.isEmpty()) Toast.makeText(activity, "Seleccione una lista", Toast.LENGTH_LONG).show()

            //TODO: definir la seleccion de una lista. Se debe implementar navegacion a lista cn el else
        }

    }

    private fun nextAction() {
        (activity as HomeActivity).showMessage("Siguiente elemento")

        elementChanging = when (elementChanging) {
            MainListItemAttributes.TITULO.name -> MainListItemAttributes.DESCRIPCION.name
            MainListItemAttributes.DESCRIPCION.name -> MainListItemAttributes.STATUS.name
            MainListItemAttributes.STATUS.name -> MainListItemAttributes.TITULO.name

            else -> MainListItemAttributes.TITULO.name
        }
        adapter.activateElement(0, elementChanging)
    }

    private fun createItemAction() {
        //Si hay creandose alguna lista se borrará
        rv_main_list.scrollToPosition(0)

        (activity as HomeActivity).showMessage("Creando")

        if (isCreatingNew) {
            listOfListsSelected.clear()
            listOfListsSelected.add(MainListItemData(Date().time, "", "", "", ""))
            deleteItemAction(listOfListsSelected)
        } else {
            isCreatingNew = true
            isSelecting = false
        }

        adapter.deactivateView(0)
        elementChanging = MainListItemAttributes.TITULO.name

        var data = MainListItemData(Date().time, "", "", "")
        adapter.insert(0, data, elementChanging)
    }

    private fun deleteItemAction(listOfSelected: ArrayList<MainListItemData>) {
        (activity as HomeActivity).showMessage("Borrando...")

        if (listOfSelected.isEmpty()) Toast.makeText(activity, "Seleccione lista", Toast.LENGTH_LONG).show()
        else {
            listOfListsSelected.forEach {
                adapter.removeByItem(it)
            }
        }
    }

    fun getLists(): ArrayList<MainListItemData> {
        var lists = ArrayList<MainListItemData>()
        lists.add(MainListItemData(Date().time, "Item 1", "List compra", "listado de compra"))
        lists.add(MainListItemData(Date().time, "Perro canibal", "List TODOs", "List TODO"))
        lists.add(MainListItemData(Date().time, "Item 3", "Lista Tareas", "Lista Tareas"))
        lists.add(MainListItemData(Date().time, "Item 4", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Item 5", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Perro listo", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Item 7", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Item 8", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Perro rojo", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Item 11", "Historial", "Historial"))
        lists.add(MainListItemData(Date().time, "Item 15", "Historial", "Historial"))
        return lists
    }
}