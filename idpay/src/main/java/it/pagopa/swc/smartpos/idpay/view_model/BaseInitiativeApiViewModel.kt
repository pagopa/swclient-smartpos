package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business

abstract class BaseInitiativeApiViewModel : ViewModel() {
    fun callList(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ) = HttpServiceInterface(viewModelScope).retrieveInitiative(context, bearer, currentBusiness)

    fun deleteTransaction(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).deleteTransaction(context, bearer, currentBusiness, transactionId)

    fun createTransaction(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: CreateTransactionRequest
    ) = HttpServiceInterface(viewModelScope).createTransaction(context, bearer, currentBusiness, request)
}