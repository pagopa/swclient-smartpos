package it.pagopa.swc_smartpos.sharedutils

import android.util.Log

/**Object to manage logs from library, normally in your activity you should put enabled=BuildConfig.DEBUG*/
object WrapperLogger {
    var enabled = false
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