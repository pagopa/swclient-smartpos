package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.model.response.CreateTransactionResponse
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChooseImportViewModel : ViewModel() {
    private val _amount = MutableStateFlow(0L)
    val amount = _amount.asStateFlow()
    private val _transaction = MutableStateFlow<CreateTransactionResponse?>(null)
    val transaction = _transaction.asStateFlow()
    fun setTransaction(transaction: CreateTransactionResponse) {
        _transaction.value = transaction
    }

    fun setAmount(value: Long) {
        _amount.value = value
    }

    fun createTransaction(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: CreateTransactionRequest
    ) = HttpServiceInterface(viewModelScope).createTransaction(context, bearer, currentBusiness, request)
}