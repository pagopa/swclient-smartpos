package it.pagopa.swc.smartpos.idpay.network

import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.view.view_shared.genericErrorDialog

/**You can use this wrapper instead of [androidx.lifecycle.Observer] to observe LiveData from network.
 * @param activity the current activity
 * @param successAction what to do on success
 * @param errorAction what to do in case of error with provided result code
 * @param showLoader show or not app loader during request
 * @param showDialog show or not dialog in case of error
 * @param showSecondScreenLoader manage to show or not second screen loader
 * @param error400 what to do in case of error400 with message sent by BE
 * @param error500 what to do in case of error500 with message sent by BE*/
class NetworkErrorMessageWrapper<Data>(
    private val activity: MainActivity?,
    successAction: (Data?) -> Unit,
    private val errorAction: ((Int?) -> Unit)? = null,
    private val showLoader: Boolean = true,
    private val showDialog: Boolean = true,
    showSecondScreenLoader: Boolean = true,
    private val error400: ((String?) -> Unit)? = null,
    private val error500: ((String?) -> Unit)? = null,
    doErrorActionOnNoNetwork: Boolean = false
) : BaseWrapper<Data>(
    activity,
    successAction,
    errorAction,
    showLoader,
    showDialog,
    showSecondScreenLoader,
    doErrorActionOnNoNetwork
) {
    override fun error(message: String?, code: Int?) {
        activity?.backPressEnabled(true)
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        when (code) {
            400 -> error400(message)
            500 -> error500(message)
            else -> mainErrorLogic(message, code)
        }
    }

    private fun error400(message: String?) {
        if (showDialog && error400 == null) {
            activity?.genericErrorDialog {
                errorAction?.invoke(400)
            }
        } else {
            if (error400 == null)
                errorAction?.invoke(400)
            else
                error400.invoke(message)
        }
    }

    private fun error500(message: String?) {
        if (showDialog && error500 == null) {
            activity?.genericErrorDialog {
                errorAction?.invoke(500)
            }
        } else {
            if (error500 == null)
                errorAction?.invoke(500)
            else
                error500.invoke(message)
        }
    }
}