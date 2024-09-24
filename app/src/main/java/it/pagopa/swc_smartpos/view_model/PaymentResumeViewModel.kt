package it.pagopa.swc_smartpos.view_model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentResumeViewModel : ActivateAndRequestFeeBaseVm() {
    private val _fromManually = MutableStateFlow(false)
    val fromManually = _fromManually.asStateFlow()
    fun setFromManually(value: Boolean) {
        _fromManually.value = value
    }
}