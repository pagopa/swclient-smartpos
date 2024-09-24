package it.pagopa.swc.smartpos.idpay

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication

class Application : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}