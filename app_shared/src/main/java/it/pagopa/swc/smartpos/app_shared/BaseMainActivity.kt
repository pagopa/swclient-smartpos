package it.pagopa.swc.smartpos.app_shared

import SdkUtility
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewbinding.ViewBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.app_shared.permission.PermissionHandler
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableProgressBar
import it.pagopa.swc.smartpos.app_shared.utils.TerminalInfoWrapper
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.encryption.decryptWith
import it.pagopa.swc_smartpos.sharedutils.encryption.encryptLongStrings
import it.pagopa.swc_smartpos.sharedutils.interfaces.SecondScreenConnection
import it.pagopa.swc_smartpos.sharedutils.model.Payment
import it.pagopa.swc_smartpos.sharedutils.model.PaymentStatus
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableState
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.setupUiKitToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.security.KeyStore

/**Base class to extend to have initial logic for the apps and get Poynt and Pax services*/
abstract class BaseMainActivity<T : ViewBinding>(val bindingFactory: (LayoutInflater) -> T) :
    AppCompatActivity() {
    abstract val navHostContainer: Int
    abstract val mockEnv: Boolean
    abstract val isPoynt: Boolean
    val splashTime = 1000L
    var sdkUtils: SdkUtility? = null
    lateinit var binding: T
    abstract val viewModel: BaseMainViewModel
    lateinit var keyStore: KeyStore
    var pb: DrawableProgressBar? = null
    abstract val loginUtility: LoginUtility
    val refreshingSessionDialog by lazy {
        UiKitStyledDialog.withStyle(Style.Info)
            .withTitle(this.resources.getString(R.string.verifying_session_title))
    }
    private val sessionExpiredDialog by lazy {
        UiKitStyledDialog.withStyle(Style.Warning)
    }

    fun updateHeaderAndLoader(
        currentMainParent: ViewGroup,
        newMainParent: ViewGroup,
        isLoader: Boolean = false
    ) {
        val (realMainParent, realNewMainParent) = if (isLoader)
            (currentMainParent.getChildAt(0) as LinearLayoutCompat) to (newMainParent.getChildAt(0) as LinearLayoutCompat)
        else
            currentMainParent to newMainParent
        for (i in 0 until realMainParent.childCount) {
            val currentChild = realMainParent.getChildAt(i)
            val newChild = realNewMainParent.getChildAt(i)
            currentChild.layoutParams = newChild.layoutParams
            currentChild.setPadding(
                newChild.paddingStart,
                newChild.paddingTop,
                newChild.paddingEnd,
                newChild.paddingBottom
            )
            if (currentChild is AppCompatTextView)
                currentChild.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    (newChild as AppCompatTextView).textSize
                )
        }
    }

    var onUpdatingView: (foldableState: FoldableState) -> Unit = {}
    var onUpdatingViewForDialog: (foldableState: FoldableState) -> Unit = {}
    open var updateActivityView: (foldableState: FoldableState) -> Unit = {}
    abstract fun setTheme()
    abstract fun setupSdkUtils()

    @CallSuper
    open fun setupOnCreate() {
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
        setupSdkUtils()
        registerIntents()
        setupObservers()
        sdkUtils?.launchBusinessInfo()
        keyStore = KeyStore.getInstance("AndroidKeyStore")
        try {
            keyStore.load(null)
        } catch (e: java.lang.Exception) {
            WrapperLogger.e("KeystoreLoad", e.message.toString())
        }
        viewModel.foldableManagement.intoCreateView(this) {
            updateActivityView.invoke(it)
            onUpdatingView.invoke(it)
            onUpdatingViewForDialog.invoke(it)
        }
        binding.root.setupUiKitToast(this, viewModel.toastMutable, viewModel.toast)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setupOnCreate()
        if (BuildConfig.DEBUG)
            WrapperLogger.i("DEVICE INFO", TerminalInfoWrapper.build().json())
    }

    abstract fun onShowingLoader(pair: Pair<Boolean, Boolean>)
    abstract fun headerViewLogic(header: HeaderView?)

    fun getCurrentFragment(): Fragment? {
        val navHostFragment: Fragment? = supportFragmentManager.findFragmentById(navHostContainer)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    fun backPressEnabled(value: Boolean) {
        (getCurrentFragment() as? BaseDataBindingFragment<*, *>)?.backPressActionEnabled?.value =
            value
    }

    private fun setupObservers() {
        viewModel.showLoader.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                (getCurrentFragment() as? BaseDataBindingFragment<*, *>)?.backPressActionEnabled?.value =
                    !(it.first || it.second)
                onShowingLoader(it)
            }
        }
        viewModel.headerView.observe(this) {
            headerViewLogic(it)
        }
        viewModel.viewModelScope.launch {
            viewModel.keepScreenOn.collectLatest {
                if (it)
                    this@BaseMainActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else
                    this@BaseMainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    @CallSuper
    open fun backToLogin() {
        viewModel.backToFirstToken()
    }

    private fun registerIntents() {
        sdkUtils?.registerPaymentLauncher {
            viewModel.setPayment(it)
        }
        sdkUtils?.registerBusinessLauncher()
    }

    @CallSuper
    open fun setupOnResume() {
        viewModel.foldableManagement.ifViewCreated()
        sdkUtils?.onUserConnected {
            sdkUtils?.getCurrentUser()
        }
        sdkUtils?.onBusinessConnected {
            sdkUtils?.refreshCurrentBusiness()
        }
        sdkUtils?.bindPrinterService {
            viewModel.setPrinterAvailable(true)
        }
        if (isPoynt) {
            if (sdkUtils?.getToken()?.value?.peekContent()?.accessToken == null)
                sdkUtils?.askForToken()
            else
                ctrlToken(sdkUtils?.getToken()?.value?.peekContent()?.accessToken)
            sdkUtils?.onSecondScreenConnected(object : SecondScreenConnection {
                override fun serviceConnected() {
                    if (Build.MANUFACTURER.equals("POYNT", true)) {
                        viewModel.setHasSecondScreen(true)
                        sdkUtils?.displayDrawable(viewModel.currentSecondScreenDrawable.value)
                    }
                }
            })
        } else
            ctrlToken(null)
    }

    override fun onResume() {
        super.onResume()
        setupOnResume()
    }

    private fun dismissRefreshingSessionDialogSafely() {
        if (refreshingSessionDialog.isVisible)
            refreshingSessionDialog.dismiss()
    }

    private fun LoginUtility.firstTokenLogic(
        isPoynt: Boolean,
        token: String?,
        sdkUtils: SdkUtility?
    ) {
        if (isPoynt && token != null) {
            this.callTokenPoynt(
                token,
                sdkUtils?.getCurrentBusiness()?.value
            ) {
                if (!it)
                    sessionExpiredDialog(token)
            }
        }
    }

    private fun LoginUtility.tokenReallyExpiredLogic(
        isPoynt: Boolean,
        token: String?,
        sdkUtils: SdkUtility?
    ) {
        dismissRefreshingSessionDialogSafely()
        if (isPoynt && token != null) {
            if (this.poyntTokenCtrl()) {
                this.callTokenPoynt(
                    token,
                    sdkUtils?.getCurrentBusiness()?.value
                ) {
                    if (!it)
                        sessionExpiredDialog(token)
                }
            } else
                sdkUtils?.askForToken()
        } else
            sessionExpiredDialog()
    }

    private fun ctrlToken(token: String?) {
        if (LoginUtility.shouldVerifySession && !sessionExpiredDialog.isVisible) {
            loginUtility.tokenCtrl(isMock = mockEnv) { actionToDo ->
                WrapperLogger.i("TokenStatus", actionToDo.name)
                when (actionToDo) {
                    LoginUtility.ActionToDo.ValidToken -> dismissRefreshingSessionDialogSafely()
                    LoginUtility.ActionToDo.FirstToken -> loginUtility.firstTokenLogic(
                        isPoynt,
                        token,
                        sdkUtils
                    )

                    LoginUtility.ActionToDo.TokenReallyExpired -> loginUtility.tokenReallyExpiredLogic(
                        isPoynt,
                        token,
                        sdkUtils
                    )

                    LoginUtility.ActionToDo.RefreshToken -> loginUtility.refreshToken {
                        dismissRefreshingSessionDialogSafely()
                        if (!it)
                            sessionExpiredDialog(token)
                    }
                }
            }
        }
    }

    fun sessionExpiredDialog(token: String? = null, action: (() -> Unit)? = null) {
        if (isPoynt) {
            sessionExpiredDialog.withTitle(getText(R.string.title_unknownError))
                .withMainBtn(getText(R.string.cta_retry)) {
                    this.viewModel.setLoaderText(
                        this.resources?.getString(R.string.feedback_loading_generic).orEmpty()
                    )
                    this.viewModel.showLoader(true to false)
                    loginUtility.callTokenPoynt(
                        token.orEmpty(),
                        sdkUtils?.getCurrentBusiness()?.value
                    ) {
                        this.viewModel.showLoader(false to false)
                        if (!it)
                            sessionExpiredDialog(token, action)
                        else
                            action?.invoke()
                    }
                }.withSecondaryBtn(getText(R.string.cta_exitApp)) {
                    this.finishAndRemoveTask()
                }
            sessionExpiredDialog.isCancelable = false
            sessionExpiredDialog.showDialog(this.supportFragmentManager)
        } else {
            sessionExpiredDialog.withTitle(getText(R.string.title_sessionExpired))
                .withDescription(getText(R.string.paragraph_sessionExpired))
                .withMainBtn(getText(R.string.cta_login)) {
                    backToLogin()
                }
            sessionExpiredDialog.isCancelable = false
            sessionExpiredDialog.showDialog(this.supportFragmentManager)
        }
    }

    fun observeLiveDataToken(onPoyntTokenAcquired: () -> Unit) {
        if (isPoynt) {
            sdkUtils?.getToken()?.observe(this) { poyntToken ->
                poyntToken?.getContentIfNotHandled()?.let { tokenByPoynt ->
                    loginUtility.callTokenPoynt(
                        tokenByPoynt.accessToken.orEmpty(),
                        sdkUtils?.getCurrentBusiness()?.value
                    ) {
                        if (!it) {
                            onPoyntTokenAcquired.invoke()
                            lifecycleScope.launch(Dispatchers.Main.immediate) {
                                delay(1000L)
                                sessionExpiredDialog(tokenByPoynt.accessToken) {}
                            }
                        } else
                            onPoyntTokenAcquired.invoke()
                    }
                }
            }
        } else
            viewModel.splashTime(splashTime, onPoyntTokenAcquired)
    }

    @CallSuper
    open fun setupOnNewIntent(intent: Intent) {
        WrapperLogger.d("Payment", intent.toString())
        val result = intent.data?.getQueryParameter("result")
        if (result != null) {
            viewModel.setPayment(
                when {
                    result.equals("APPROVED", true) -> Payment("", PaymentStatus.COMPLETED)
                    result.equals("DECLINED", true) -> Payment("", PaymentStatus.DECLINED)
                    else -> Payment("", PaymentStatus.CANCELED)
                }
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setupOnNewIntent(intent)
    }

    open fun setupOnPause() {
        LoginUtility.shouldVerifySession = true
        sdkUtils?.unBindServices()
        if (viewModel.hasSecondScreen.value) {
            pb?.cancelJob()
            sdkUtils?.resetWelcomeScreen()
        }
    }

    override fun onPause() {
        setupOnPause()
        super.onPause()
    }

    /**This f uses a lambda due to usage of CoroutineScope(Dispatchers.Default)
     * @see [kotlinx.coroutines.Dispatchers.Default]*/
    fun encrypt(what: String, encryptedList: (ArrayList<String?>) -> Unit) {
        this.encryptLongStrings(keyStore, what) {
            var finalJob: Job? = null
            finalJob = CoroutineScope(Dispatchers.Main).launch {
                encryptedList.invoke(it)
                finalJob?.cancel()
            }
            finalJob.start()
        }
    }

    /**This f uses a lambda due to usage of CoroutineScope(Dispatchers.Default)
     * @see [kotlinx.coroutines.Dispatchers.Default]*/
    fun decrypt(what: ArrayList<String?>?, onDecrypted: (String) -> Unit) {
        what.decryptWith(keyStore) {
            var finalJob: Job? = null
            finalJob = CoroutineScope(Dispatchers.Main).launch {
                onDecrypted.invoke(it)
                finalJob?.cancel()
            }
            finalJob.start()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHandler.REQUEST_PERMISSION) {
            when {
                grantResults.all { it == PackageManager.PERMISSION_GRANTED } -> PermissionHandler.pCallback?.permissionGranted()
                permissions.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        it
                    )
                } -> PermissionHandler.pCallback?.neverAskAgainClicked()

                else -> PermissionHandler.pCallback?.permissionDenied()
            }
        }
    }
}