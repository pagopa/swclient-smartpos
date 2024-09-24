package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.model.response.HistoricalTransactionsResponse
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionHistoryViewModel : ViewModel() {

    private val _transactions = MutableStateFlow<HistoricalTransactionsResponse?>(null)
    val transactions = _transactions.asStateFlow()
    fun setTransactions(list: HistoricalTransactionsResponse?) {
        _transactions.value = list
    }

    fun callTransactionsHistory(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ) = HttpServiceInterface(viewModelScope).getHistoricalTransactions(context, bearer, currentBusiness)
}