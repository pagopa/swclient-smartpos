package it.pagopa.swc_smartpos

import it.pagopa.swc.smartpos.app_shared.BaseMainViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : BaseMainViewModel() {
    private val _receiptModel = MutableStateFlow(ReceiptModel())
    val receiptModel = _receiptModel.asStateFlow()
    private val _transactionId = MutableStateFlow("")
    val transactionId = _transactionId.asStateFlow()
    private val _subscriberId = MutableStateFlow("")
    val subscriberId = _subscriberId.asStateFlow()
    private val _deactivateHelpedWay = MutableStateFlow(false)
    val deactivateHelpedWay = _deactivateHelpedWay.asStateFlow()

    fun setHelpedWayDeactivated(value: Boolean) {
        _deactivateHelpedWay.value = value
    }

    fun setReceiptModel(model: ReceiptModel) {
        receiptModel.value.setModel(receiptModel, model)
    }

    fun generateTransactionId() {
        val allowedCharsSecond = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val id = (1..32)
            .map { allowedCharsSecond.random() }
            .joinToString("")
        _transactionId.value = id
    }

    fun setHelpedWay(value: String) {
        _subscriberId.value = value
    }
}