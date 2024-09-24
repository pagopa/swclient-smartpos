package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.view.InsertManuallyFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InsertManuallyViewModel : ViewModel() {
    private val _buttonVisible = MutableLiveData(false)
    val buttonVisible: LiveData<Boolean> = _buttonVisible
    private val _buttonModel = MutableLiveData(InsertManuallyFragment.ButtonModel(false) {})
    val buttonModel: LiveData<InsertManuallyFragment.ButtonModel> = _buttonModel
    private val _backPressAction = MutableStateFlow {}
    val backPressAction = _backPressAction.asStateFlow()
    private val _adviseCode = MutableStateFlow("")
    val adviseCode = _adviseCode.asStateFlow()
    private val _creditorFiscalCode = MutableStateFlow("")
    val creditorFiscalCode = _creditorFiscalCode.asStateFlow()
    private val _state = MutableStateFlow(InsertManuallyFragment.State.AdviseCode)
    val state = _state.asStateFlow()

    fun setButtonModel(model: InsertManuallyFragment.ButtonModel) {
        _buttonModel.postValue(model)
    }

    fun setButtonVisibility(value: Boolean) {
        _buttonVisible.postValue(value)
    }

    fun setState(state: InsertManuallyFragment.State) {
        _state.value = state
    }

    fun setBackPressAction(action: () -> Unit) {
        _backPressAction.value = action
    }

    fun setAdviseCode(value: String) {
        _adviseCode.value = value
    }

    fun setCreditorFiscalCode(value: String) {
        _creditorFiscalCode.value = value
    }

    fun verifyPayment(
        context: Context,
        paTaxCode: String,
        noticeNumber: String,
        bearer: String,
        currentBusiness: Business?
    ) = HttpServiceInterface(viewModelScope).verifyPayment(context, paTaxCode, noticeNumber, bearer, currentBusiness)
}