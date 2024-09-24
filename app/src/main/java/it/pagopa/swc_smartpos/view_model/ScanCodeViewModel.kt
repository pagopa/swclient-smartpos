package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business

class ScanCodeViewModel : ViewModel() {
    fun verifyQrCode(
        context: Context,
        qrCode: String,
        bearer: String,
        currentBusiness: Business?
    ) = HttpServiceInterface(viewModelScope).verifyQrCode(context, qrCode, bearer, currentBusiness)
}