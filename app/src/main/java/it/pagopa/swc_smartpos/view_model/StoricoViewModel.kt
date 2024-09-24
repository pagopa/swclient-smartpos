package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.model.HistoricalTransactionResponse
import it.pagopa.swc_smartpos.model.Transaction
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.sharedutils.model.Business

class StoricoViewModel : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>?>(null)
    val transactions: LiveData<List<Transaction>?> = _transactions
    fun setTransactions(list: List<Transaction>) {
        _transactions.postValue(list)
    }

    fun callTransactionsHistory(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<HistoricalTransactionResponse>> {
        return HttpServiceInterface(viewModelScope).getHistoricalTransactions(context = context, bearer = bearer, currentBusiness)
    }
}