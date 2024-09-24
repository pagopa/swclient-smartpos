package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.pagopa.readcie.NisAuthenticated
import it.pagopa.swc.smartpos.app_shared.BaseReadCie
import it.pagopa.swc.smartpos.idpay.model.request.VerifyCieRequest
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.view.cie_read_or_qr_code.CieReaderOrQrCodeFragment
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CieReaderOrQrCodeViewModel : BaseInitiativeApiViewModel() {
    private val _citizenSituation = MutableStateFlow<String?>(TransactionStatus.CREATED.name)
    val citizenSituation = _citizenSituation.asStateFlow()
    private val _uiModel = MutableStateFlow(CieReaderOrQrCodeFragment.UiModel())
    val uiModel = _uiModel.asStateFlow()
    private val _cieRead = MutableStateFlow(VerifyCieRequest())
    val cieRead = _cieRead.asStateFlow()
    private val _nisAuthenticated = MutableLiveData<BaseReadCie.FunInterfaceResource<NisAuthenticated?>?>(null)
    val nisAuthenticated: LiveData<BaseReadCie.FunInterfaceResource<NisAuthenticated?>?> = _nisAuthenticated
    private val _transmitting = MutableLiveData(false)
    val transmitting: LiveData<Boolean> = _transmitting

    fun void() {
        _citizenSituation.value = TransactionStatus.CREATED.name
        _cieRead.value = VerifyCieRequest()
        _nisAuthenticated.value = null
        _transmitting.value = false
    }

    fun setCitizenSituation(value: String?) {
        if (value == TransactionStatus.CREATED.name && citizenSituation.value == TransactionStatus.IDENTIFIED.name)
            _citizenSituation.value = "From Identified"
        else
            _citizenSituation.value = value
    }

    fun setUiModel(value: CieReaderOrQrCodeFragment.UiModel?) {
        _uiModel.value = value ?: CieReaderOrQrCodeFragment.UiModel()
    }

    fun setCieRead(value: VerifyCieRequest) {
        _cieRead.value = value
    }

    fun setTransmitting(value: Boolean) {
        _transmitting.postValue(value)
    }

    fun setNisAuthenticated(nisAuthenticated: BaseReadCie.FunInterfaceResource<NisAuthenticated?>?) {
        _nisAuthenticated.postValue(nisAuthenticated)
    }

    fun verifyCie(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: VerifyCieRequest,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).verifyCie(context, bearer, currentBusiness, request, transactionId)

    fun idPayTransactionDetail(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ) = HttpServiceInterface(viewModelScope).idPayTransactionDetail(context, bearer, currentBusiness, transactionId)

}