package it.pagopa.swc.smartpos.idpay.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast

class LoginViewModel : ViewModel() {
    private val _inputOne = MutableLiveData<String?>(null)
    val inputOne: LiveData<String?> = _inputOne
    private val _inputTwo = MutableLiveData<String?>(null)
    val inputTwo: LiveData<String?> = _inputTwo
    val toastMutable = MutableLiveData<UiKitToast?>(null)
    val toast: LiveData<UiKitToast?> = toastMutable
    fun setInputOne(value: String) {
        _inputOne.postValue(value)
    }

    fun setInputTwo(value: String) {
        _inputTwo.postValue(value)
    }

    fun setToast(toast: UiKitToast) {
        toastMutable.postValue(toast)
    }

    fun doLogin(
        context: Context,
        request: LoginRequest,
        currentBusiness: Business?
    ) = HttpServiceInterface(viewModelScope).login(context, request, currentBusiness)

    fun clear(){
        _inputOne.postValue(null)
        _inputTwo.postValue(null)
    }
}