package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.view.ResidualPaymentFragment
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResidualPaymentViewModel : ViewModel() {
    private val _model = MutableStateFlow<ResidualPaymentFragment.ResidualPaymentModel?>(null)
    val model = _model.asStateFlow()
    private val _payWithCashChosen = MutableStateFlow(false)
    val payWithCashChosen = _payWithCashChosen.asStateFlow()
    private val _paidWithCard = MutableStateFlow(false)
    val paidWithCard = _paidWithCard.asStateFlow()
    fun setModel(model: ResidualPaymentFragment.ResidualPaymentModel?) {
        _model.value = model
    }

    fun setPayWithCashChosen(value: Boolean) {
        _payWithCashChosen.value = value
    }

    fun setPaidWithCard(value: Boolean) {
        _paidWithCard.value = value
    }

    fun cancelOp(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).deleteTransaction(context, bearer, currentBusiness, transactionId)
}