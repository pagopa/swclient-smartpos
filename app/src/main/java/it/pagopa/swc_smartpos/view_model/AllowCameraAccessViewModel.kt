package it.pagopa.swc_smartpos.view_model

import androidx.lifecycle.ViewModel
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseAllowCameraAccessFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AllowCameraAccessViewModel : ViewModel() {
    private val _state = MutableStateFlow(BaseAllowCameraAccessFragment.State.First)
    val state = _state.asStateFlow()
    fun setState(state: BaseAllowCameraAccessFragment.State) {
        _state.value = state
    }
}