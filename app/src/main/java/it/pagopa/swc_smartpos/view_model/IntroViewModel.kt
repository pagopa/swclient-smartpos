package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.Event
import it.pagopa.swc_smartpos.sharedutils.model.Business

class IntroViewModel : ActivateAndRequestFeeBaseVm() {
    private val _subscriberId = MutableLiveData<Event<String>>(null)
    val subscriberId: LiveData<Event<String>> = _subscriberId
    fun setSubscriberId(value: String) {
        _subscriberId.postValue(Event(value))
    }

    fun getLastOperation(
        context: Context,
        bearer: String,
        business: Business?,
        subscriberId: String
    ) = HttpServiceInterface(viewModelScope).getLastPresetOperation(context, bearer, business, business?.paTaxCode.orEmpty(), subscriberId)

    fun verify(
        context: Context,
        paTaxCode : String,
        noticeNumber : String,
        bearer: String,
        currentBusiness : Business
    ) = HttpServiceInterface(viewModelScope).verifyPayment(context, paTaxCode, noticeNumber, bearer, currentBusiness)
}