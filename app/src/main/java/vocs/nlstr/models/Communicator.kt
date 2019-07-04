package vocs.nlstr.models

import android.arch.lifecycle.ViewModel
import java.util.*

class Communicator : ViewModel() {

    val message = ArrayList<MainListItemData>()

    fun setList(list: ArrayList<MainListItemData>) {
        message.addAll(list)
    }
}