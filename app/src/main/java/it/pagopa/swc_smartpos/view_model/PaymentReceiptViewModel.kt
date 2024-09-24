package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentReceiptViewModel : BasePaymentViewModel() {
    private val _currentBusiness = MutableStateFlow<Business?>(null)
    private val currentBusiness = _currentBusiness.asStateFlow()
    private val _amount = MutableStateFlow<PaymentReceiptFragment.Model?>(null)
    val amount = _amount.asStateFlow()
    override val mBusiness: Business?
        get() = currentBusiness.value

    fun setAmount(amount: PaymentReceiptFragment.Model?) {
        amount?.let { _amount.value = it }
    }

    fun setCurrentBusiness(business: Business?) {
        business?.let {
            _currentBusiness.value = it
        }
    }

    fun closePaymentPolling(
        context: Context,
        bearer: String,
        url: String
    ) = HttpServiceInterface(viewModelScope).closePaymentPolling(
        context,
        bearer,
        currentBusiness.value,
        url
    )

    fun closePaymentPollingManually(
        context: Context,
        bearer: String,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).closePaymentPollingManually(
        context,
        bearer,
        currentBusiness.value,
        transactionId
    )
}