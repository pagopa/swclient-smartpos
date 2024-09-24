package it.pagopa.swc_smartpos.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalRequest
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelpedWaySubscribeViewModel : ViewModel() {
    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()
    fun setInput(value: String) {
        _input.value = value
    }

    fun subscribeTerminal(
        context: Context,
        bearer: String,
        request: SubscribeTerminalRequest,
        business: Business?
    ) = HttpServiceInterface(viewModelScope).subscribeTerminal(context, bearer, request, business)
}