package it.pagopa.swc_smartpos.network

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.BaseResponse
import it.pagopa.swc_smartpos.second_screen.showResultToSecondScreenRespectApiErrorCode
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.view.ResultFragment

/**You can use this wrapper instead of [androidx.lifecycle.Observer] to observe LiveData from network.
 * @param activity the current activity
 * @param successAction what to do on success
 * @param errorAction what to do in case of error with provided result code
 * @param showLoader show or not app loader during request
 * @param showDialog show or not dialog in case of error
 * @param isForQrCode manage qr code cases*/
class NetworkWrapper<Data : BaseResponse>(
    private val activity: MainActivity?,
    private val successAction: (Data?) -> Unit,
    private val errorAction: ((Int?) -> Unit)? = null,
    private val showLoader: Boolean = true,
    private val showDialog: Boolean = true,
    private val isForQrCode: Boolean = false,
    showSecondScreenLoader: Boolean = true,
    doErrorActionOnNoNetwork: Boolean = false
) : BaseWrapper<Data>(
    activity,
    successAction,
    errorAction,
    showLoader,
    showDialog,
    showSecondScreenLoader,
    doErrorActionOnNoNetwork
), NetworkObserver<Data> {
    override fun error(message: String?, code: Int?) {
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        var dialogAlreadyLaunched = false
        if (isForQrCode && code == 400 && (message?.contains("008000001") == true
                    || message?.contains("00800002B") == true)
        ) {
            dialogAlreadyLaunched = true
            UiKitStyledDialog.withStyle(Style.Warning)
                .withMainBtn(getText(R.string.cta_retry)) {
                    errorAction?.invoke(400)
                }
                .withTitle(getText(R.string.title_invalidQrCode))
                .withDescription(getText(R.string.paragraph_invalidQrCode))
                .withSecondaryBtn(getText(R.string.cta_enterManually)) {
                    errorAction?.invoke(5050)
                }
                .showDialog(activity?.supportFragmentManager)
            activity showResultToSecondScreenRespectApiErrorCode "400"
        }
        if (showDialog) {
            if (!dialogAlreadyLaunched) {
                super.error(message, code)
            }
        } else
            errorAction?.invoke(code)
    }

    override fun error200(outcome: String) {
        if (showLoader)
            activity?.viewModel?.showLoader(false to false)
        if (isForQrCode) {
            when (outcome) {
                Api.ErrorCode.NOTICE_GLITCH -> globalToResult(
                    BaseResultFragment.State.Error,
                    R.string.title_technicalError,
                    R.string.paragraph_contactSupport
                )

                Api.ErrorCode.WRONG_NOTICE_DATA -> globalToResult(
                    BaseResultFragment.State.Warning,
                    R.string.title_invalidData,
                    R.string.paragraph_contactSupport
                )

                Api.ErrorCode.CREDITOR_PROBLEMS -> globalToResult(
                    BaseResultFragment.State.Error,
                    R.string.title_payeeTimeout,
                    R.string.paragraph_contactSupport
                )

                Api.ErrorCode.PAYMENT_ALREADY_IN_PROGRESS -> globalToResult(
                    BaseResultFragment.State.Warning,
                    R.string.title_paymentOngoing,
                    R.string.paragraph_contactSupport
                )

                Api.ErrorCode.EXPIRED_NOTICE -> globalToResult(
                    BaseResultFragment.State.Warning,
                    R.string.title_expiredNotice,
                    R.string.paragraph_contactPayee
                )

                Api.ErrorCode.UNKNOWN_NOTICE -> globalToResult(
                    BaseResultFragment.State.Warning,
                    R.string.title_unknownNotice,
                    R.string.paragraph_unknownNotice
                )

                Api.ErrorCode.REVOKED_NOTICE -> globalToResult(
                    BaseResultFragment.State.Warning,
                    R.string.title_revokedNotice,
                    R.string.paragraph_contactPayee
                )

                Api.ErrorCode.NOTICE_ALREADY_PAID -> globalToResult(
                    BaseResultFragment.State.Info,
                    R.string.paragraph_noticeAlreadyPaid
                )

                else -> globalToResult(
                    BaseResultFragment.State.Error,
                    R.string.title_unknownError,
                    R.string.paragraph_contactSupport
                )
            }
            activity showResultToSecondScreenRespectApiErrorCode outcome
        }
        errorAction?.invoke(200)
    }

    private fun globalToResult(
        state: BaseResultFragment.State,
        @StringRes title: Int,
        @StringRes description: Int? = null
    ) {
        activity?.findNavController(R.id.nav_host_container)
            ?.navigate(R.id.action_global_resultFragment, Bundle().apply {
                putBoolean(ResultFragment.backHome, true)
                this.putSerializable(BaseResultFragment.stateArg, state)
                this.putInt(BaseResultFragment.titleArg, title)
                this.putInt(BaseResultFragment.descriptionArg, description ?: 0)
                this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer(
                    getText(R.string.cta_goToHomepage),
                    null, true
                ) { it.findNavController().popBackStack(R.id.introFragment, false) })
            })
    }
}