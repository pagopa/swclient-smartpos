package it.pagopa.swc.smartpos.app_shared

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc_smartpos.sharedutils.Event
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.model.Payment
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableManagement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseMainViewModel : ViewModel() {
    var keepSplashOnScreen = true
    var animateIcon = true
    val toastMutable = MutableLiveData<UiKitToast?>(null)
    val toast: LiveData<UiKitToast?> = toastMutable
    val foldableManagement = FoldableManagement(WrapperLogger.enabled)
    private val _payment = MutableLiveData<Payment?>(null)
    val payment: LiveData<Payment?> = _payment
    private val _isPrinterAvailable = MutableStateFlow(false)
    val isPrinterAvailable = _isPrinterAvailable.asStateFlow()
    private val _accessToken = MutableStateFlow<ArrayList<String?>?>(arrayListOf(""))
    val accessToken = _accessToken.asStateFlow()
    private val _refreshToken = MutableStateFlow<ArrayList<String?>?>(arrayListOf(""))
    val refreshToken = _refreshToken.asStateFlow()
    private val _hasSecondScreen = MutableStateFlow(false)
    val hasSecondScreen = _hasSecondScreen.asStateFlow()
    private val _currentSecondScreenDrawable = MutableStateFlow<Drawable?>(null)
    val currentSecondScreenDrawable = _currentSecondScreenDrawable.asStateFlow()
    private val _showLoader = MutableLiveData(Event(Pair(false, false)))
    val showLoader: LiveData<Event<Pair<Boolean, Boolean>>> = _showLoader
    private val _loaderText = MutableStateFlow("")
    val loaderText = _loaderText.asStateFlow()
    private val _headerView = MutableLiveData<HeaderView?>(null)
    val headerView: LiveData<HeaderView?> = _headerView
    private val _keepScreenOn = MutableStateFlow(false)
    val keepScreenOn = _keepScreenOn.asStateFlow()
    fun setToast(toast: UiKitToast) {
        toastMutable.postValue(toast)
    }

    fun setKeepScreenOn(value: Boolean) {
        _keepScreenOn.value = value
    }

    fun setPayment(value: Payment?) {
        LoginUtility.shouldVerifySession = false
        _payment.postValue(value)
    }

    fun setPrinterAvailable(value: Boolean) {
        _isPrinterAvailable.value = value
    }

    fun backToFirstToken() {
        _accessToken.value = arrayListOf("")
    }

    fun setAccessToken(value: ArrayList<String?>?) {
        _accessToken.value = value
    }

    fun setRefreshToken(value: ArrayList<String?>?) {
        _refreshToken.value = value
    }

    fun setHasSecondScreen(value: Boolean) {
        _hasSecondScreen.value = value
    }

    fun setSecondScreenDrawable(drawable: Drawable?) {
        _currentSecondScreenDrawable.value = drawable
    }

    fun showLoader(showIt: Pair<Boolean, Boolean>) {
        _showLoader.value = Event(showIt)
    }

    fun setHeaderView(value: HeaderView?) {
        _headerView.postValue(value)
    }

    fun splashTime(delay: Long, action: () -> Unit) {
        viewModelScope.launch {
            delay(delay)
            action.invoke()
        }
    }

    fun setLoaderText(text: String) {
        _loaderText.value = text
    }
}