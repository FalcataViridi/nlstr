package vocs.nlstr.servicios

import android.content.Context
import vocs.nlstr.interfaces.DatabaseCallback


class DatabaseManager(private val context: Context
                      , private val callback: DatabaseCallback? = null
) //: Listener
{



    init {
        setDatabasePreferences()
        initializeCall()
    }

    private fun initializeCall() {

    }


    private fun setDatabasePreferences() { }

    private fun preInitialize() { }
}
