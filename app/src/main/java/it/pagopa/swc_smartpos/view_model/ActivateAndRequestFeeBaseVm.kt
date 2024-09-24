package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentRequest
import it.pagopa.swc_smartpos.model.fee.RequestFeeRequest
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ActivateAndRequestFeeBaseVm : BasePaymentViewModel() {
    private val _preCloseRequest = MutableStateFlow<PreCloseRequest.PresetRequest?>(null)
    val preCloseRequest = _preCloseRequest.asStateFlow()
    private val _paymentTokens = MutableStateFlow<List<String>?>(null)
    val paymentTokens = _paymentTokens.asStateFlow()
    private val _toGenerateTransactionId = MutableStateFlow(true)
    val toGenerateTransactionId = _toGenerateTransactionId.asStateFlow()
    private val _currentBusiness = MutableStateFlow<Business?>(null)
    private val currentBusiness = _currentBusiness.asStateFlow()
    private val _qrCodeVerify = MutableStateFlow<QrCodeVerifyResponse?>(null)
    val qrCodeVerify = _qrCodeVerify.asStateFlow()
    override val mBusiness: Business?
        get() = currentBusiness.value

    fun setPreCloseRequest(req: PreCloseRequest.PresetRequest?) {
        _preCloseRequest.value = req
    }
    fun setCurrentBusiness(business: Business?) {
        business?.let {
            _currentBusiness.value = it
        }
    }

    fun setQrCodeVerifyModel(qrCodeVerifyResponse: QrCodeVerifyResponse?) {
        qrCodeVerifyResponse?.let {
            _qrCodeVerify.value = it
        }
    }

    fun setToGenerateTransactionId(value: Boolean) {
        _toGenerateTransactionId.value = value
    }

    fun setPaymentTokens(list: List<String>) {
        _paymentTokens.value = list
    }

    fun addPaymentToken(value: String) {
        if (_paymentTokens.value != null)
            _paymentTokens.value = _paymentTokens.value!! + value
    }

    fun generateIdempotencyKey(onEnd: (String) -> Unit) {
        val allowedCharsFirst = ('0'..'9')
        val first = (1..11)
            .map { allowedCharsFirst.random() }
            .joinToString("")
        val allowedCharsSecond = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val second = (1..10)
            .map { allowedCharsSecond.random() }
            .joinToString("")
        onEnd.invoke("${first}_$second")
    }

    fun activatePayment(
        context: Context,
        qrCode: String,
        bearer: String,
        request: ActivatePaymentRequest
    ) = HttpServiceInterface(viewModelScope).activatePayment(context, qrCode, bearer, mBusiness, request)

    fun activateManualPayment(
        context: Context,
        paTaxCode: String,
        noticeNumber: String,
        bearer: String,
        request: ActivatePaymentRequest
    ) = HttpServiceInterface(viewModelScope).activateManualPayment(context, paTaxCode, noticeNumber, bearer, mBusiness, request)

    fun requestFee(
        context: Context,
        bearer: String,
        request: RequestFeeRequest
    ) = HttpServiceInterface(viewModelScope).requestFee(context, bearer, mBusiness, request)
}