package it.pagopa.swc.smartpos.app_shared.network

import androidx.annotation.CallSuper
import com.google.android.material.snackbar.Snackbar
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.R
import it.pagopa.swc_smartpos.sharedutils.extensions.orEmptyCharSeq
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import it.pagopa.swc_smartpos.ui_kit.utils.Style

/**You can use this wrapper instead of [androidx.lifecycle.Observer] to observe LiveData from network.
 * **P.N.**: This is a **BaseWrapper** so it's an open class, you can inherit it and specialize your class like in samples
 * @param activity the current activity as [it.pagopa.swc.smartpos.app_shared.BaseMainActivity]
 * @param successAction what to do on success
 * @param errorAction what to do in case of error with provided result code
 * @param showLoader show or not app loader during request
 * @param showDialog show or not dialog in case of error
 * @param showSecondScreenLoader manage to show or not second screen loader
 * @sample[it.pagopa.swc_smartpos.network.NetworkWrapper]
 * @sample[it.pagopa.swc.smartpos.idpay.network.NetworkErrorMessageWrapper]
 * @author with ❤️ by Carlo De Chellis, Mobilesoft*/
open class BaseWrapper<T>(
    private val activity: BaseMainActivity<*>?,
    private val successAction: (T?) -> Unit,
    private val errorAction: ((Int?) -> Unit)? = null,
    private val showLoader: Boolean = true,
    private val showDialog: Boolean = true,
    private val showSecondScreenLoader: Boolean = true,
    private val doErrorActionOnNoNetwork: Boolean = false
) : BaseObserver<T> {

    @CallSuper
    override fun loading() {
        activity?.backPressEnabled(false)
        if (showLoader)
            activity?.viewModel?.showLoader(true to showSecondScreenLoader)
    }

    @CallSuper
    override fun success(t: T?) {
        activity?.backPressEnabled(true)
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        successAction.invoke(t)
    }

    fun mainErrorLogic(message: String?, code: Int?) {
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        if (code == null && message.equals("no network available", true)) {
            activity?.viewModel?.setToast(
                UiKitToast(
                    UiKitToast.Value.Warning,
                    getText(R.string.feedback_no_network),
                    Snackbar.LENGTH_LONG
                )
            )
            if (doErrorActionOnNoNetwork)
                errorAction?.invoke(NO_NETWORK)
        } else {
            if (showDialog && code != tokenRefreshed) {
                UiKitStyledDialog.withMainBtn(getText(R.string.cta_okay)).withDismissAction {
                    errorAction?.invoke(code)
                }.withStyle(Style.Error).withTitle(getText(R.string.title_unknownError))
                    .withDescription(getText(R.string.paragraph_contactSupport))
                    .showDialog(activity?.supportFragmentManager)
            } else
                errorAction?.invoke(code)
        }
    }

    override fun error(message: String?, code: Int?) {
        activity?.backPressEnabled(true)
        mainErrorLogic(message, code)
    }

    override fun error401() {
        activity?.backPressEnabled(true)
        activity?.loginUtility?.refreshTokenCtrl { isValid ->
            if (isValid) {
                activity.loginUtility.refreshToken { callOk ->
                    if (!callOk)
                        tokenReallyExpired()
                    else
                        errorAction?.invoke(tokenRefreshed)
                }
            } else
                tokenReallyExpired()
        }
    }

    private fun tokenReallyExpired() {
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        if (activity?.isPoynt == true) {
            activity.loginUtility.callTokenPoynt(
                activity.sdkUtils?.getToken()?.value?.peekContent()?.accessToken.orEmpty(),
                activity.sdkUtils?.getCurrentBusiness()?.value
            ) {
                if (!it)
                    activity.sessionExpiredDialog(activity.sdkUtils?.getToken()?.value?.peekContent()?.accessToken)
                else
                    errorAction?.invoke(tokenRefreshed)
            }
        } else {
            activity?.sessionExpiredDialog()
        }
    }

    fun getText(id: Int) = activity?.resources?.getText(id).orEmptyCharSeq()

    companion object {
        const val tokenRefreshed = 1234
        const val NO_NETWORK = 11111
    }
}