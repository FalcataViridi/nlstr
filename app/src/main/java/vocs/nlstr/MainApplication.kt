package vocs.nlstr

import android.app.Application
import butterknife.ButterKnife
import timber.log.Timber

/**
 * Created by Moises on 20/12/2017.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.e(this.toString() + " - ENTRANDO")
    }
}
