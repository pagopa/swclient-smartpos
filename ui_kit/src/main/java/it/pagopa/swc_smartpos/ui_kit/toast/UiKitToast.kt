package it.pagopa.swc_smartpos.ui_kit.toast

import com.google.android.material.snackbar.Snackbar

data class UiKitToast(val value: Value, val text: CharSequence, val timeLength: Int = Snackbar.LENGTH_SHORT, val showImage: Boolean = true) {
    enum class Value {
        Generic,
        Success,
        Info,
        Warning,
        Error
    }
}