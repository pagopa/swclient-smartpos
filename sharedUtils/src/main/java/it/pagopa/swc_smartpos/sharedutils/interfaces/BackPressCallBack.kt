package it.pagopa.swc_smartpos.sharedutils.interfaces

import androidx.activity.OnBackPressedCallback

internal class BackPressCallBack(private val callback: BackPressAction) : OnBackPressedCallback(true) {
    fun interface BackPressAction {
        fun action()
    }

    override fun handleOnBackPressed() {
        callback.action()
    }
}
