package it.pagopa.swc.smartpos.idpay.view.view_shared

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.Application
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.ChooseInitiative
import it.pagopa.swc.smartpos.idpay.view.ResultFragment
import it.pagopa.swc.smartpos.idpay.view_model.BaseInitiativeApiViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.orEmptyCharSeq
import it.pagopa.swc_smartpos.sharedutils.interfaces.PrintStatus
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc.smartpos.app_shared.R as RShared

private fun getText(activity: MainActivity?, id: Int) =
    activity?.resources?.getText(id).orEmptyCharSeq()

fun MainActivity?.genericErrorDialog(errorAction: (() -> Unit)? = null) {
    UiKitStyledDialog.withMainBtn(getText(this, RShared.string.cta_okay)).withDismissAction {
        errorAction?.invoke()
    }.withStyle(Style.Error).withTitle(getText(this, RShared.string.title_unknownError))
        .withDescription(getText(this, RShared.string.paragraph_contactSupport))
        .showDialog(this?.supportFragmentManager)
}

fun Fragment.accessTokenLambda(action: (String) -> Unit) {
    val mainActivity = activity as? MainActivity
    mainActivity?.backPressEnabled(false)
    mainActivity?.decrypt(mainActivity.viewModel.accessToken.value, action)
}

fun BaseDataBindingFragmentApp<*>.idPayOpNavigateSuccess(howMuchPaidWithIdPay: String) {
    this.findNavController().navigate(
        R.id.action_global_resultFragment,
        bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Success,
            BaseResultFragment.titleArg to R.string.id_pay_bonus_ok_descr,
            BaseResultFragment.titleArgumentConstant to howMuchPaidWithIdPay,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.cta_continue),
                it.pagopa.swc_smart_pos.ui_kit.R.drawable.arrow_right,
                false
            ) {
                it.findNavController().navigate(R.id.action_resultFragment_to_receiptFragment)
            },
            ResultFragment.operationState to ResultFragment.OperationState.OK
        )
    )
}

fun BaseDataBindingFragment<*, MainActivity>.acceptNewBonus(viewModel: BaseInitiativeApiViewModel) {
    mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
    mainActivity?.viewModel?.showLoader(true to false)
    accessTokenLambda { bearer ->
        viewModel.callList(
            mainActivity!!,
            bearer,
            mainActivity?.sdkUtils?.getCurrentBusiness()?.value
        ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
            mainActivity?.viewModel?.voidModel()
            setFragmentResult(
                ChooseInitiative.initiativesArgResultKey,
                bundleOf(ChooseInitiative.initiativesArg to it)
            )
            if (!findNavController().popBackStack(R.id.chooseInitiative, false)) {
                clearFragmentResult(ChooseInitiative.initiativesArgResultKey)
                findNavController().navigate(
                    R.id.action_global_choose_initiative,
                    bundleOf(ChooseInitiative.initiativesArg to it)
                )
            }
        }, errorAction = {
            if (it == BaseWrapper.tokenRefreshed)
                this.acceptNewBonus(viewModel)
            else
                findNavController().navigate(R.id.action_global_resultFragment, Bundle().apply {
                    putBoolean(ResultFragment.backHome, true)
                    this.putSerializable(
                        BaseResultFragment.stateArg,
                        BaseResultFragment.State.Error
                    )
                    this.putInt(
                        BaseResultFragment.titleArg,
                        R.string.title_errorLoadingTransactionList
                    )
                    this.putInt(BaseResultFragment.descriptionArg, R.string.paragraph_tryAgain)
                    this.putSerializable(
                        BaseResultFragment.firstButtonArg,
                        CustomBtnCustomizer(getTextSafely(RShared.string.cta_goToHomepage)) { frag ->
                            frag.findNavController().popBackStack(R.id.introFragment, false)
                        })
                })
        }, showLoader = true, showSecondScreenLoader = false, showDialog = false))
    }
}

fun BaseDataBindingFragment<*, MainActivity>.errorDialog(mainBtnAction: (() -> Unit)? = null) {
    UiKitStyledDialog.withMainBtn(getTextSafely(RShared.string.cta_okay)).withDismissAction {
        mainBtnAction?.invoke()
    }.withStyle(Style.Error).withTitle(getText(RShared.string.title_unknownError))
        .withDescription(getText(RShared.string.paragraph_contactSupport))
        .showDialog(activity?.supportFragmentManager)
}

fun BaseDataBindingFragment<*, MainActivity>.errorDialogWithMoreDescr(
    moreDescr: String? = null,
    mainBtnAction: (() -> Unit)? = null
) {
    val descr: CharSequence = if (moreDescr != null)
        "${getText(RShared.string.paragraph_contactSupport)}\n$moreDescr"
    else
        getText(RShared.string.paragraph_contactSupport)
    UiKitStyledDialog.withMainBtn(getTextSafely(RShared.string.cta_okay)).withDismissAction {
        mainBtnAction?.invoke()
    }.withStyle(Style.Error).withTitle(getText(RShared.string.title_unknownError))
        .withDescription(descr)
        .showDialog(activity?.supportFragmentManager)
}

fun MainActivity?.print(
    receiptDrawable: Pair<Drawable?, Int>,
    showSecondScreenLoader: Boolean,
    actionQueued: (() -> Unit)? = null
) {
    val (receipt, receiptHeight) = receiptDrawable
    if (BuildConfig.FLAVOR.contains("androidNative", true)) {
        receipt?.let {
            this?.sdkUtils?.printDrawable(
                Application.instance.applicationContext,
                it,
                receiptHeight,
                object : PrintStatus {
                    override fun queued() {
                        actionQueued?.invoke()
                    }

                    override fun failed() {}
                })
        }
    } else {
        if (this?.viewModel?.isPrinterAvailable?.value == true) {
            this.resources?.getString(R.string.feedback_loading_printReceipt)?.let {
                this.viewModel.setLoaderText(it)
                this.viewModel.showLoader(true to showSecondScreenLoader)
            }
            receipt?.let {
                this.sdkUtils?.printDrawable(
                    Application.instance.applicationContext,
                    it,
                    receiptHeight,
                    object : PrintStatus {
                        override fun queued() {
                            this@print.runOnUiThread {
                                this@print.viewModel.showLoader(false to false)
                                actionQueued?.invoke()
                            }
                        }

                        override fun failed() {
                            this@print.runOnUiThread {
                                this@print.viewModel.showLoader(false to false)
                                UiKitStyledDialog.withStyle(Style.Warning)
                                    .withTitle(
                                        this@print.resources?.getText(RShared.string.title_printingError)
                                            .orEmptyCharSeq()
                                    )
                                    .withDescription(
                                        this@print.resources?.getText(RShared.string.paragraph_printingError)
                                            .orEmptyCharSeq()
                                    )
                                    .withMainBtn(
                                        this@print.resources?.getText(RShared.string.cta_retry)
                                            .orEmptyCharSeq()
                                    ) {
                                        print(receiptDrawable, showSecondScreenLoader, actionQueued)
                                    }.withSecondaryBtn(
                                        this@print.resources?.getText(R.string.cta_close)
                                            .orEmptyCharSeq()
                                    )
                                    .showDialog(this@print.supportFragmentManager)
                            }
                            WrapperLogger.e("Printer", "FAILED")
                        }
                    })
            } ?: run {
                WrapperLogger.e("Printer", "FAILED due to null bitmap")
            }
        } else {
            UiKitStyledDialog.withStyle(Style.Warning)
                .withTitle(
                    this@print?.resources?.getText(RShared.string.title_printingError)
                        .orEmptyCharSeq()
                )
                .withDescription(
                    this@print?.resources?.getText(RShared.string.paragraph_printingError)
                        .orEmptyCharSeq()
                )
                .withMainBtn(
                    this@print?.resources?.getText(RShared.string.cta_retry).orEmptyCharSeq()
                ) {
                    print(receiptDrawable, showSecondScreenLoader, actionQueued)
                }.withSecondaryBtn(
                    this@print?.resources?.getText(RShared.string.cta_close).orEmptyCharSeq()
                )
                .showDialog(this@print?.supportFragmentManager)
        }
    }
}