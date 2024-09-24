package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.idpay.model.request.AuthorizeRequest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.view.InsertCiePinFragment
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InsertCiePinViewModel : ViewModel() {
    var pinAttempts = 3
    private val _uiModel = MutableStateFlow<InsertCiePinFragment.UiModel?>(null)
    val uiModel = _uiModel.asStateFlow()
    private val _pin = MutableStateFlow("")
    val pin = _pin.asStateFlow()

    fun setUiModel(model: InsertCiePinFragment.UiModel?) {
        _uiModel.value = model
    }

    fun setPin(value: String) {
        _pin.value = value
    }

    fun authorize(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: AuthorizeRequest,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).authorize(context, bearer, currentBusiness, request, transactionId)


    fun cancelOp(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).deleteTransaction(context, bearer, currentBusiness, transactionId)
}