package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.printer.PrintReceipt
import it.pagopa.swc.smartpos.idpay.view.transaction_detail.TransactionDetailUiModel
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionDetailViewModel : ViewModel() {
    var isAndroidNativeShare = false
    var isFromPause = false to false
    private val _uiModel = MutableStateFlow<TransactionDetailUiModel?>(null)
    val uiModel = _uiModel.asStateFlow()
    fun setUiModel(uiModel: TransactionDetailUiModel?) {
        _uiModel.value = uiModel
    }

    fun print(mainActivity: MainActivity?): Pair<Drawable?, Int>? {
        val instance = PrintReceipt(mainActivity)
        val transaction = uiModel.value?.transaction ?: return null
        return when (transaction.status) {
            TransactionStatus.AUTHORIZED.name, TransactionStatus.REWARDED.name -> instance.receiptOkDrawable(
                SaleModel(
                    Initiatives.InitiativeModel(
                        transaction.initiativeId.orEmpty(), transaction.initiativeId.orEmpty(), ""
                    ),
                    transaction.goodsCost,
                    transaction.coveredAmount,
                    100f,
                    transaction.milTransactionId,
                    transaction.lastUpdate,
                    false
                )
            ) to instance.receiptHeight

            else -> instance.receiptNotOkDrawable(
                SaleModel(
                    Initiatives.InitiativeModel(
                        transaction.initiativeId.orEmpty(), transaction.initiativeId.orEmpty(), ""
                    ),
                    transaction.goodsCost,
                    transaction.coveredAmount,
                    100f,
                    transaction.milTransactionId,
                    transaction.lastUpdate,
                    true
                )
            ) to instance.receiptHeight
        }
    }

    fun cancelOp(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).deleteTransaction(context, bearer, currentBusiness, transactionId)
}