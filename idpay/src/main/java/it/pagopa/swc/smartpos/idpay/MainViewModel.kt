package it.pagopa.swc.smartpos.idpay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.pagopa.readcie.NisAuthenticated
import it.pagopa.swc.smartpos.app_shared.BaseMainViewModel
import it.pagopa.swc.smartpos.app_shared.BaseReadCie
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : BaseMainViewModel() {
    private val _nisAuthenticated =
        MutableLiveData<BaseReadCie.FunInterfaceResource<NisAuthenticated?>?>(null)
    val nisAuthenticated: LiveData<BaseReadCie.FunInterfaceResource<NisAuthenticated?>?> =
        _nisAuthenticated
    private var keyBackStack: Any? = null
    fun <T> setKeyBackStack(value: T?) {
        keyBackStack = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <Obj> checkKeyBackStackWithPair(key:String): Boolean {
        return try {
            val newBackStack = keyBackStack as Pair<String, Obj>
            return newBackStack.first == key
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getKeyBackStack(): T? {
        val value = keyBackStack as? T
        keyBackStack = null
        return value
    }

    private val _transmitting = MutableLiveData(false)
    val transmitting: LiveData<Boolean> = _transmitting
    private val _model = MutableStateFlow(SaleModel())
    val model = _model.asStateFlow()
    fun setModel(value: SaleModel) {
        model.value.set(model, value)
    }

    fun voidModel() {
        model.value.voidOp()
    }

    fun setTransmitting(value: Boolean) {
        _transmitting.postValue(value)
    }

    fun setNisAuthenticated(nisAuthenticated: BaseReadCie.FunInterfaceResource<NisAuthenticated?>?) {
        _nisAuthenticated.postValue(nisAuthenticated)
    }
}