package it.pagopa.swc_smartpos.view_model

import androidx.lifecycle.ViewModel
import it.pagopa.swc_smartpos.sharedutils.extensions.launchTimer

class OutroViewModel : ViewModel() {
    fun oneMinuteMaxInFragment(onEnd: () -> Unit) {
        this.launchTimer(60, onEnd)
    }
}