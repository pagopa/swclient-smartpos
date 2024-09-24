package it.pagopa.swc_smartpos.view_model.receipt_model

import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import kotlinx.coroutines.flow.StateFlow

data class ReceiptModel(
    var state: BaseResultFragment.State? = null,
    var labelDateAndTime: String? = null,
    var labelPayee: String? = null,
    var labelPayeeTaxCode: String? = null,
    var labelNoticeCode: String? = null,
    var labelPaymentReason: String? = null,
    var labelAmount: String? = null,
    var labelFee: String? = null,
    var labelTotalAmount: String? = null,
    var transactionID: String? = null,
    var labelTerminalCode: String? = null,
    var paymentToken: String? = null
) : java.io.Serializable {
    private fun <T> setParamCorrectly(current: T?, param: T?): T? {
        return when {
            current == null -> param
            current != param && param != null -> param
            else -> current
        }
    }

    fun setModel(currentFlow: StateFlow<ReceiptModel>, value: ReceiptModel) {
        state = setParamCorrectly(currentFlow.value.state, value.state)
        labelDateAndTime = setParamCorrectly(currentFlow.value.labelDateAndTime, value.labelDateAndTime)
        labelPayee = setParamCorrectly(currentFlow.value.labelPayee, value.labelPayee)
        labelPayeeTaxCode = setParamCorrectly(currentFlow.value.labelPayeeTaxCode, value.labelPayeeTaxCode)
        labelNoticeCode = setParamCorrectly(currentFlow.value.labelNoticeCode, value.labelNoticeCode)
        labelPaymentReason = setParamCorrectly(currentFlow.value.labelPaymentReason, value.labelPaymentReason)
        labelAmount = setParamCorrectly(currentFlow.value.labelAmount, value.labelAmount)
        labelFee = setParamCorrectly(currentFlow.value.labelFee, value.labelFee)
        labelTotalAmount = setParamCorrectly(currentFlow.value.labelTotalAmount, value.labelTotalAmount)
        transactionID = setParamCorrectly(currentFlow.value.transactionID, value.transactionID)
        labelTerminalCode = setParamCorrectly(currentFlow.value.labelTerminalCode, value.labelTerminalCode)
    }
}