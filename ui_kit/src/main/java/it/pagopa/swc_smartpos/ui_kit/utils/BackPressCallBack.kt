package it.pagopa.swc_smartpos.ui_kit.utils

import androidx.activity.OnBackPressedCallback

class BackPressCallBack(private val callback: BackPressAction) : OnBackPressedCallback(true) {
    fun interface BackPressAction {
        fun action()
    }

    override fun handleOnBackPressed() {
        callback.action()
    }
}