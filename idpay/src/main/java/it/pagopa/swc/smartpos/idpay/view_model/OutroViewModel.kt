package it.pagopa.swc.smartpos.idpay.view_model

import it.pagopa.swc_smartpos.sharedutils.extensions.launchTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OutroViewModel : BaseInitiativeApiViewModel() {
    private val _payWithCashChosen = MutableStateFlow(false)
    val payWithCashChosen = _payWithCashChosen.asStateFlow()
    var isFromDetail = false
    fun setPayWithCashChosen(value: Boolean) {
        _payWithCashChosen.value = value
    }

    fun oneMinuteMaxInFragment(onEnd: () -> Unit) {
        this.launchTimer(60, onEnd)
    }
}