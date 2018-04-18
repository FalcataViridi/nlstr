package vocs.nlstr

import android.app.Application
import android.widget.Toast
import butterknife.ButterKnife
import timber.log.Timber

/**
 * Created by Moises on 20/12/2017.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Toast.makeText(this, "...INICIO", Toast.LENGTH_LONG)
    }

    private fun toast() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


