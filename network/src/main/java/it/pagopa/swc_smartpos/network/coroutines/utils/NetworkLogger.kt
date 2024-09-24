package it.pagopa.swc_smartpos.network.coroutines.utils

import android.util.Log
import it.pagopa.swc_smartpos.network.BuildConfig

/**Object to manage logs from library, normally in your activity you should put enabled=BuildConfig.DEBUG*/
object NetworkLogger {
    var enabled = false
    var isMockEnv = BuildConfig.FLAVOR.equals("mock", true)
    fun v(tag: String, message: String) {
        if (enabled)
            Log.v(tag, message)
    }

    fun e(tag: String, message: String) {
        if (enabled)
            Log.e(tag, message)
    }

    fun d(tag: String, message: String) {
        if (enabled)
            Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (enabled)
            Log.i(tag, message)
    }
}