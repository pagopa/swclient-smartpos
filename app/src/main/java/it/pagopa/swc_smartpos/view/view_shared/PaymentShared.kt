package it.pagopa.swc_smartpos.view.view_shared

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentRequest
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentResponse
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.model.preclose.PreCloseResponse
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import it.pagopa.swc_smartpos.view.PaymentResumeFragment
import it.pagopa.swc_smartpos.view.ResultFragment
import it.pagopa.swc_smartpos.view_model.BasePaymentViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.doClosePaymentKo(
    failed: Boolean = true,
    maxRetries: Int = 3
) {
    mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.feedback_loading_generic))
    mainActivity?.viewModel?.showLoader(true to true)
    accessTokenLambda { accessToken ->
        (this as? PaymentResumeFragment)
            ?: ((this as? PaymentReceiptFragment)?.viewModel?.closePayment(
                mainActivity!!, accessToken,
                ClosePaymentRequest(
                    Status.ERROR_ON_PAYMENT.status,
                    SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date().time),
                ),
                mainActivity?.viewModel?.transactionId?.value.orEmpty(),
            )?.observeForKOForClosePaymentWith(this, viewModel, maxRetries, failed))
    }
}


fun <Fragment : BaseDataBindingFragment<*, MainActivity>> Fragment.doAbortPreClosePayment(maxRetries: Int = 3) {
    when (this) {
        is PaymentReceiptFragment -> {
            viewModel.amount.value?.let { mModel ->
                mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.feedback_loading_generic))
                mainActivity?.viewModel?.showLoader(true to true)
                accessTokenLambda { accessToken ->
                    val fee = mModel.fee
                    val paymentTokens = mModel.paymentTokens
                    val transactionId =
                        mainActivity?.viewModel?.receiptModel?.value?.transactionID ?: ""
                    viewModel.preClose(
                        mainActivity!!,
                        accessToken,
                        PreCloseRequest(
                            fee = fee,
                            outcome = Status.ABORT.status,
                            paymentTokens = paymentTokens,
                            totalAmount = mModel.amount,
                            transactionId = transactionId
                        )
                    ).observeForKOForPreClosePaymentWith(this, viewModel, maxRetries)
                }
            } ?: run {
                backToIntroFragment()
            }
        }

        is PaymentResumeFragment -> {
            viewModel.paymentTokens.value?.let { listPaymentTokens ->
                mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.feedback_loading_generic))
                mainActivity?.viewModel?.showLoader(true to true)
                accessTokenLambda { accessToken ->
                    val fee =
                        mainActivity?.viewModel?.receiptModel?.value?.labelFee?.replace(",", ".")
                            ?.replace(" €", "")?.toIntOrNull() ?: 0
                    val transactionId =
                        mainActivity?.viewModel?.receiptModel?.value?.transactionID ?: ""
                    val totalAmount =
                        mainActivity?.viewModel?.receiptModel?.value?.labelAmount?.replace(
                            ",",
                            ""
                        )?.replace(" €", "")?.toIntOrNull() ?: 0
                    viewModel.preClose(
                        mainActivity!!,
                        accessToken,
                        PreCloseRequest(
                            fee = fee,
                            outcome = Status.ABORT.status,
                            paymentTokens = listPaymentTokens,
                            totalAmount = totalAmount,
                            transactionId = transactionId
                        )
                    ).observeForKOForPreClosePaymentWith(this, viewModel, maxRetries)
                }
            } ?: run {
                backToIntroFragment()
            }
        }
    }
}


private fun LiveData<Resource<ClosePaymentResponse>>.observeForKOForClosePaymentWith(
    owner: BaseDataBindingFragmentApp<*>,
    viewModel: BasePaymentViewModel,
    maxRetries: Int,
    failed: Boolean = true
) {
    this.observe(owner.viewLifecycleOwner, BaseWrapper(owner.activity as? MainActivity, {
        if (failed) owner.goToFailedTransaction() else owner.goToCanceledTransaction()
    }, {
        //retry after 15 seconds
        val retryAfter = 15
        var maxRetriesHere = maxRetries
        val now = Date().time / 1000
        if (it == BaseWrapper.tokenRefreshed)
            owner.doClosePaymentKo(failed = failed, maxRetriesHere)
        else {
            val nowHere = Date().time / 1000
            if (maxRetriesHere == 0) {
                //navigate To Result
                if (failed) owner.goToFailedTransaction() else owner.goToCanceledTransaction()
            } else {
                maxRetriesHere--
                viewModel.vmDelay(retryAfter - (nowHere.toInt() - now.toInt())) {
                    owner.doClosePaymentKo(failed = failed, maxRetriesHere)
                }
            }
        }
    }, showDialog = false, showLoader = false))
}

private fun LiveData<Resource<PreCloseResponse>>.observeForKOForPreClosePaymentWith(
    owner: BaseDataBindingFragmentApp<*>,
    viewModel: BasePaymentViewModel,
    maxRetries: Int
) {
    this.observe(owner.viewLifecycleOwner, BaseWrapper(owner.mainActivity, {
        owner.backToIntroFragment()
        owner.mainActivity?.viewModel?.showLoader(false to false)
    }, {
        //retry after 15 seconds
        val retryAfter = 15
        var maxRetriesHere = maxRetries
        val now = Date().time / 1000
        if (it == BaseWrapper.tokenRefreshed)
            owner.doAbortPreClosePayment(maxRetries)
        else {
            if (it != BaseWrapper.NO_NETWORK)
                maxRetriesHere--
            val nowHere = Date().time / 1000
            if (maxRetriesHere == 0) {
                //navigate To Result
                owner.backToIntroFragment()
                owner.mainActivity?.viewModel?.showLoader(false to false)
            } else {
                viewModel.vmDelay(retryAfter - (nowHere.toInt() - now.toInt())) {
                    owner.doAbortPreClosePayment(maxRetriesHere)
                }
            }

        }
    }, showDialog = false, showLoader = false, doErrorActionOnNoNetwork = true))
}

fun <Fragment : BaseDataBindingFragment<*, MainActivity>> Fragment.errorDialogAndClosePayment() {
    UiKitStyledDialog.withMainBtn(getStringSafely(R.string.cta_okay)).withDismissAction {
        when (this) {
            is PaymentResumeFragment -> binding.btnPay.showLoading(false)
            is PaymentReceiptFragment -> binding.btnPayAmount.showLoading(false)
        }
        this.doAbortPreClosePayment()
    }.withStyle(Style.Error).withTitle(getText(R.string.title_unknownError))
        .withDescription(getText(R.string.paragraph_contactSupport))
        .showDialog(activity?.supportFragmentManager)
}


fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.globalToResult(
    state: BaseResultFragment.State,
    @StringRes title: Int,
    @StringRes description: Int? = null,
    firstButton: CustomBtnCustomizer? = null,
    secondButton: CustomBtnCustomizer? = null,
    isErrorAndCanceled: Boolean = true
) {
    navigate(R.id.action_global_resultFragment, Bundle().apply {
        //putBoolean(NetworkWrapper.wrapperRecognition, true)
        this.putSerializable(BaseResultFragment.stateArg, state)
        this.putInt(BaseResultFragment.titleArg, title)
        this.putInt(BaseResultFragment.descriptionArg, description ?: 0)
        this.putSerializable(BaseResultFragment.firstButtonArg, firstButton)
        this.putSerializable(BaseResultFragment.secondButtonArg, secondButton)
        this.putBoolean(ResultFragment.isErrorAndCanceledArg, isErrorAndCanceled)
    })
}


fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.goToFailedTransaction() {
    this.mainActivity?.viewModel?.showLoader(false to false)
    globalToResult(
        BaseResultFragment.State.Error,
        R.string.title_transactionCancelled,
        R.string.paragraph_noCharges,
        CustomBtnCustomizer(getTextSafely(R.string.cta_newPayment)) {
            it.findNavController().popBackStack(R.id.introFragment, false)
        }, null, true
    )
}

fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.goToCanceledTransaction() {
    this.mainActivity?.viewModel?.showLoader(false to false)
    globalToResult(
        BaseResultFragment.State.Error,
        R.string.title_authorizationDenied,
        R.string.paragraph_noCharges,
        CustomBtnCustomizer(getTextSafely(R.string.cta_newPayment)) {
            it.findNavController().popBackStack(R.id.introFragment, false)
        }, null, false
    )
}


fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.goToUnknownOutcome() {
    mainActivity?.viewModel?.showLoader(false to false)
    mainActivity?.viewModel?.setReceiptModel(ReceiptModel(state = BaseResultFragment.State.Info))
    globalToResult(
        BaseResultFragment.State.Info,
        R.string.title_uncertainOutcome,
        R.string.paragraph_contactPagopa,
        CustomBtnCustomizer(getTextSafely(R.string.cta_sendEmail), R.drawable.mail_white, true) {
        },
        CustomBtnCustomizer(
            getTextSafely(R.string.cta_printReceipt),
            R.drawable.print_info_dark,
            true
        ) {
            if (it.findNavController().currentDestination?.id == R.id.resultFragment)
                (it.activity as? MainActivity)?.printWithMainViewModel(Status.PENDING) {
                    it.findNavController().navigate(R.id.action_resultFragment_to_outroFragment)
                }
        }
    )
}

fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.goToTechnicalError() {
    mainActivity?.viewModel?.showLoader(false to false)
    mainActivity?.viewModel?.setReceiptModel(ReceiptModel(state = BaseResultFragment.State.Warning))
    globalToResult(
        BaseResultFragment.State.Warning,
        R.string.title_koOutcome,
        R.string.paragraph_contactPagopa,
        CustomBtnCustomizer(getTextSafely(R.string.cta_sendEmail), R.drawable.mail_white, true) {
        },
        CustomBtnCustomizer(
            getTextSafely(R.string.cta_printReceipt),
            R.drawable.print_warning_dark,
            true
        ) {
            if (it.findNavController().currentDestination?.id == R.id.resultFragment)
                (it.activity as? MainActivity)?.printWithMainViewModel(Status.ERROR_ON_CLOSE) {
                    it.findNavController().navigate(R.id.action_resultFragment_to_outroFragment)
                }
        }
    )
}

fun <Fragment : BaseDataBindingFragmentApp<*>> Fragment.goToPaymentCompleted() {
    mainActivity?.viewModel?.showLoader(false to false)
    mainActivity?.viewModel?.setReceiptModel(ReceiptModel(state = BaseResultFragment.State.Success))
    globalToResult(
        BaseResultFragment.State.Success,
        R.string.title_paymentCompleted,
        description = null,
        CustomBtnCustomizer(
            getTextSafely(R.string.cta_continue),
            it.pagopa.swc_smart_pos.ui_kit.R.drawable.arrow_right,
            false
        ) {
            if (it.findNavController().currentDestination?.id == R.id.resultFragment)
                it.findNavController().navigate(R.id.action_resultFragment_to_receiptFragment)
        }
    )
}
