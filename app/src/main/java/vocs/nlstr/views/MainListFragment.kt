package vocs.nlstr.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_list_main.*
import vocs.nlstr.MainApplication
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
    var commandList = ArrayList<String>()
    var textResult: String = ""
    var reconManager: RecognitionManager? = null
    var isSelecting = false
    var selectedItems = ArrayList<Int>()
    var actions = ArrayList<String>()
    var currentSubaction = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configPermission()
        initView()
        actions.add("INICIO")
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
        activateAction(key)
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


        when (actions.last()) {
            MainListKeys.CREAR.key -> {
                updatingItem(textResult)
            }

            MainListKeys.SELECCIONAR.key -> {
                listOfListsSelected.clear()
                var index = 0
                listOfLists.forEach {
                    if (it.name.toLowerCase().contains(textResult.trim())) {
                        listOfListsSelected.add(listOfLists[index])
                    }
                    index++
                }
                adapter.selectItems(listOfListsSelected)
            }

            else -> (activity as HomeActivity).showMessage("$textResult?")
        }
    }

    fun updatingItem(text: String) {
        when (elementChanging) {
            MainListItemAttributes.TITULO.name -> adapter.updateInfo(
                    0
                    , text)
            MainListItemAttributes.DESCRIPCION.name -> adapter.updateInfo(
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

    fun activateAction(key: String) {

        if ((activity as HomeActivity).isCommandshown) {
            (activity as HomeActivity).hideCommands()
        }

        when (key) {
            MainListKeys.CREAR.key -> createItemAction()
            MainListKeys.SIGUIENTE.key -> nextAction()
            MainListKeys.ACEPTAR.key -> acceptAction()
            MainListKeys.SELECCIONAR.key -> selectAction()
            MainListKeys.BORRAR.key -> deleteRowAction(listOfListsSelected)
            MainListKeys.COMANDOS.key -> showCommands()

            else -> (activity as HomeActivity).showMessage("$key?")
        }
    }

    private fun showCommands() {

        when (actions.last()) {
            MainListKeys.CREAR.key, MainListKeys.SIGUIENTE.key -> {
                commandList.clear()
                commandList.add(MainListKeys.CREAR.key)
                commandList.add(MainListKeys.SIGUIENTE.key)
                commandList.add(MainListKeys.ACEPTAR.key)
                commandList.add(MainListKeys.COMANDOS.key)
                commandList.add(MainListKeys.BORRAR.key)
            }

            MainListKeys.ACEPTAR.key -> {
                commandList.clear()
                commandList.add(MainListKeys.CREAR.key)
                commandList.add(MainListKeys.COMANDOS.key)
            }

            MainListKeys.BORRAR.key -> {
                commandList.clear()
                commandList.add(MainListKeys.CREAR.key)
                commandList.add(MainListKeys.SIGUIENTE.key)
                commandList.add(MainListKeys.ACEPTAR.key)
                commandList.add(MainListKeys.COMANDOS.key)
            }
        }

        //Show/hide commands
        if ((activity as HomeActivity).isCommandshown) {
            currentSubaction = ""
            (activity as HomeActivity).hideCommands()
        } else {
            currentSubaction = MainListKeys.COMANDOS.key
            (activity as HomeActivity).showCommands(commandList)
        }
    }

    private fun selectAction() {
        (activity as HomeActivity).showMessage("Seleccionando...", true)

        //TODO: borrar este listado tras las pruebas
        addAllListToSelected(listOfLists)

        adapter.selectItems(listOfLists)
        actions.add(MainListKeys.SELECCIONAR.key)
    }

    //TODO: borrar despues de pruebas
    private fun addAllListToSelected(listComplete: ArrayList<MainListItemData>) {
        var index = 0
        listComplete.forEach {
            selectedItems.add(index++)
        }
    }

    private fun acceptAction() {
        (activity as HomeActivity).showMessage("Seleccionado?")

        when (actions.last()) {
            MainListKeys.SELECCIONAR.key -> {
                if (selectedItems.size > 1) Toast.makeText(activity, "Seleccione una sola lista", Toast.LENGTH_LONG).show()
                else if (selectedItems.isEmpty()) Toast.makeText(activity, "No se ha seleccionado nada", Toast.LENGTH_LONG).show()
            }

            MainListKeys.CREAR.key -> {
                adapter.deactivateView(0)
                listOfListsSelected.clear()
                actions.add(MainListKeys.SELECCIONAR.key)
            }
        }
    }

    private fun nextAction() {
        (activity as HomeActivity).showMessage("Siguiente ")

        elementChanging = when (elementChanging) {
            MainListItemAttributes.TITULO.name -> MainListItemAttributes.DESCRIPCION.name
            MainListItemAttributes.DESCRIPCION.name -> MainListItemAttributes.STATUS.name
            MainListItemAttributes.STATUS.name -> MainListItemAttributes.TITULO.name

            else -> MainListItemAttributes.TITULO.name
        }
        (activity as HomeActivity).showMessage("Cambiar $elementChanging?", true)
        adapter.activateElement(0, elementChanging)

        currentSubaction = MainListKeys.SIGUIENTE.key
    }

    private fun createItemAction() {
        rv_main_list.scrollToPosition(0)
        when (actions.last()) {
            MainListKeys.CREAR.key -> {
                deleteRowAction(listOfListsSelected)
                listOfListsSelected.clear()
            }

            MainListKeys.COMANDOS.key -> {
                (activity as HomeActivity).hideCommands()

                actions.add(MainListKeys.CREAR.key)
                adapter.deactivateView(0)
                elementChanging = MainListItemAttributes.TITULO.name
            }

            else -> {
                actions.add(MainListKeys.CREAR.key)
                adapter.deactivateView(0)
                elementChanging = MainListItemAttributes.TITULO.name
            }
        }

        var data = MainListItemData(Date().time, "nombre", "Yokese", "Descripcion")
        adapter.insert(0, data, elementChanging)

        listOfListsSelected.add(data)


        (activity as HomeActivity).showMessage("...Creada")
        adapter.activateElement(0, elementChanging)
    }

    private fun deleteRowAction(listOfSelected: ArrayList<MainListItemData>) {
        (activity as HomeActivity).showMessage("Borrando...", true)

        if (listOfSelected.isEmpty()) Toast.makeText(activity, "Seleccione lista", Toast.LENGTH_LONG).show()
        else {
            listOfListsSelected.forEach {
                adapter.removeByItem(it)
                actions.add(MainListKeys.BORRAR.key)
            }
        }

        (activity as HomeActivity).showMessage("...Borrado")
    }

    fun getLists(): ArrayList<MainListItemData> {
        var lists = ArrayList<MainListItemData>()
        lists.add(MainListItemData(Date().time, "Item 1", "List compra", "listado de compra"))
        lists.add(MainListItemData(Date().time, "Perro 3 4 5 6", "List TODOs", "List TODO"))
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