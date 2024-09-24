package it.pagopa.swc.smartpos.idpay.view_model

import androidx.lifecycle.ViewModel
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChooseInitiativeViewModel : ViewModel() {
    private val _initiatives = MutableStateFlow<Initiatives?>(null)
    val initiatives = _initiatives.asStateFlow()
    fun setInitiatives(value: Initiatives?) {
        _initiatives.value = value
    }
}