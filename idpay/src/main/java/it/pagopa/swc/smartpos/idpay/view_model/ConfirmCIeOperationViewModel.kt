package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.view.ConfirmCieOperation
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfirmCIeOperationViewModel : ViewModel() {
    private val _uiModel = MutableStateFlow<ConfirmCieOperation.UiModel?>(null)
    val uiModel = _uiModel.asStateFlow()

    fun setUiModel(uiModel: ConfirmCieOperation.UiModel?) {
        _uiModel.value = uiModel
    }

    fun cancelOp(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).deleteTransaction(context, bearer, currentBusiness, transactionId)
}