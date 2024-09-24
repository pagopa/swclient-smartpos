package it.pagopa.swc_smartpos.view_model

import androidx.lifecycle.ViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReceiptViewModel : ViewModel() {
    private val _model = MutableStateFlow(ReceiptModel())
    val receiptModel = _model.asStateFlow()
    fun setReceiptModel(value: ReceiptModel?) {
        value?.let {
            _model.value = it
        }
    }
}