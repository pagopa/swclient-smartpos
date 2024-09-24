package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentRequest
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BasePaymentViewModel : ViewModel() {
    abstract val mBusiness: Business?
    fun closePayment(
        context: Context,
        bearer: String,
        closePaymentRequest: ClosePaymentRequest,
        transactionId : String,
    ) = HttpServiceInterface(viewModelScope).closePayment(context, bearer, mBusiness, closePaymentRequest, transactionId)


    fun preClose(
        context: Context,
        bearer: String,
        request: PreCloseRequest
    ) = HttpServiceInterface(viewModelScope).preClose(context, bearer, mBusiness, request)


    fun vmDelay(sec: Int, actionDone: () -> Unit) {
        viewModelScope.launch {
            delay((sec * 1000).toLong())
            actionDone.invoke()
        }
    }

}