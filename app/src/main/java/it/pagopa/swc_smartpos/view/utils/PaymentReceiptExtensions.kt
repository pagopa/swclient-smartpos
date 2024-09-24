package it.pagopa.swc_smartpos.view.utils

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentRequest
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentResponse
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.model.preclose.PreCloseResponse
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view.view_shared.errorDialogAndClosePayment
import it.pagopa.swc_smartpos.view.view_shared.goToPaymentCompleted
import it.pagopa.swc_smartpos.view.view_shared.goToTechnicalError
import it.pagopa.swc_smartpos.view.view_shared.goToUnknownOutcome
import it.pagopa.swc_smartpos.view_model.BasePaymentViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun PaymentReceiptFragment.callPreClose(
    maxRetries: Int = 3,
    amount: Long,
    totalAmount: Long,
    presetRequest: PreCloseRequest.PresetRequest? = null
) {
    accessTokenLambda { accessToken ->
        val fee = viewModel.amount.value?.fee ?: 0
        val paymentTokens = viewModel.amount.value?.paymentTokens ?: listOf()
        val transactionId = mainActivity?.viewModel?.transactionId?.value ?: ""
        viewModel.preClose(
            mainActivity!!,
            accessToken,
            PreCloseRequest(
                fee = fee,
                outcome = Status.PRE_CLOSE.status,
                paymentTokens = paymentTokens,
                totalAmount = amount.toInt(),
                transactionId = transactionId,
                preset = presetRequest
            )
        ).observePreClose(this, viewModel, maxRetries, amount, totalAmount, presetRequest)
    }
}


@SuppressLint("VisibleForTests")
private fun LiveData<Resource<PreCloseResponse>>.observePreClose(
    owner: PaymentReceiptFragment,
    viewModel: BasePaymentViewModel,
    maxRetries: Int,
    amount: Long,
    totalAmount: Long,
    presetRequest: PreCloseRequest.PresetRequest?
) {
    this.observe(owner.viewLifecycleOwner, BaseWrapper(owner.mainActivity, {
        if (owner.mainActivity?.mockEnv == true) {
            owner.dialog.loading(true)
            owner.dialog.showDialog(owner.mainActivity?.supportFragmentManager)
        }
        owner.mainActivity?.sdkUtils?.launchPayment(totalAmount, "payment_app")
    }, {
        //retry after 15 seconds
        val retryAfter = 15
        var maxRetriesHere = maxRetries
        val now = Date().time / 1000
        if (it == BaseWrapper.tokenRefreshed)
            owner.callPreClose(maxRetriesHere, amount, totalAmount, presetRequest)
        else {
            if (it != BaseWrapper.NO_NETWORK)
                maxRetriesHere--
            val nowHere = Date().time / 1000
            if (maxRetriesHere == 0)
                owner.errorDialogAndClosePayment()
            else {
                viewModel.vmDelay(retryAfter - (nowHere.toInt() - now.toInt())) {
                    owner.callPreClose(maxRetriesHere, amount, totalAmount, presetRequest)
                }
            }
        }
    }, showDialog = false, showLoader = false, doErrorActionOnNoNetwork = true))
}

fun PaymentReceiptFragment.doClosePayment(mModel: PaymentReceiptFragment.Model) {
    mainActivity?.viewModel?.setReceiptModel(
        ReceiptModel(
            labelDateAndTime = SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(Date().time),
            labelAmount = mModel.amount.toAmountFormatted()
        )
    )
    accessTokenLambda { accessToken ->
        viewModel.closePayment(
            mainActivity!!, accessToken,
            ClosePaymentRequest(
                "CLOSE",
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.getDefault()
                ).format(Date().time)
            ), mainActivity?.viewModel?.transactionId?.value.orEmpty()
        ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, { response ->
            if (response?.outcome?.equals("ok", true) == true) {
                callPolling(
                    false,
                    mainActivity!!.viewModel.transactionId.value,
                    response,
                    (response.maxRetries?.get(0) ?: 3) - 1
                ) {
                    manageResponsePolling(it)
                }
            } else {
                callPolling(
                    true,
                    transactionId = mainActivity?.viewModel?.transactionId?.value.orEmpty(),
                    maxRetries = 3
                ) { itsOk ->
                    manageResponsePolling(itsOk)
                }
            }
        }, {
            if (it == BaseWrapper.tokenRefreshed)
                doClosePayment(mModel)
            else {
                callPolling(
                    true,
                    transactionId = mainActivity?.viewModel?.transactionId?.value.orEmpty(),
                    maxRetries = 3
                ) { itsOk ->
                    manageResponsePolling(itsOk)
                }
            }
        }, showLoader = false, showDialog = false))
    }
}


private fun PaymentReceiptFragment.manageResponsePolling(b: Boolean) {
    if (this.mainActivity?.mockEnv == true) {
        this.dialog.loading(true)
        if (this.dialog.isVisible)
            this.dialog.dismiss()
    }
    mainActivity?.viewModel?.showLoader(false to false)
    if (b) {
        goToPaymentCompleted()
    } else {
        goToUnknownOutcome()
    }
}

private fun PaymentReceiptFragment.callPolling(
    manually: Boolean = false,
    transactionId: String,
    closePaymentResponse: ClosePaymentResponse? = null,
    maxRetries: Int,
    onDone: (Boolean) -> Unit
) {
    if (manually) doPollingManually(transactionId, maxRetries, onDone) else
        closePaymentResponse?.let {
            if (it.location?.get(0) != null)
                doPolling(transactionId, it, maxRetries, onDone)
            else
                doPollingManually(transactionId, maxRetries, onDone)
        }
}

private fun PaymentReceiptFragment.continueManualPolling(
    maxRetriesHere: Int,
    now: Long,
    transactionId: String,
    onDone: (Boolean) -> Unit
) {
    val retryAfter = 15
    val nowHere = Date().time / 1000
    if (maxRetriesHere == 0) {
        dismissDialogWaiting()
        onDone.invoke(false)
    } else {
        showDialogWaiting()
        viewModel.vmDelay(retryAfter - (nowHere.toInt() - now.toInt())) {
            doPollingManually(
                transactionId,
                maxRetriesHere,
                onDone
            )
        }
    }
}

private fun PaymentReceiptFragment.doPollingManually(
    transactionId: String,
    maxRetries: Int,
    onDone: (Boolean) -> Unit
) {
    var maxRetriesHere = maxRetries
    val now = Date().time / 1000
    accessTokenLambda { accessToken ->
        viewModel.closePaymentPollingManually(
            mainActivity!!,
            accessToken,
            transactionId
        ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, {
            dismissDialogWaiting()
            it?.let { mResponse ->
                when (mResponse.status) {
                    Status.CLOSED.status -> onDone.invoke(true)
                    Status.ERROR_ON_CLOSE.status, Status.ERROR_ON_RESULT.status -> goToTechnicalError()
                    else -> {
                        maxRetriesHere--
                        continueManualPolling(maxRetriesHere, now, transactionId, onDone)
                    }
                }
            } ?: run {
                maxRetriesHere--
                continueManualPolling(maxRetriesHere, now, transactionId, onDone)
            }
        }, {
            if (it == BaseWrapper.tokenRefreshed)
                doPollingManually(transactionId, maxRetriesHere, onDone)
            else {
                if (it != BaseWrapper.NO_NETWORK)
                    maxRetriesHere--
                continueManualPolling(maxRetriesHere, now, transactionId, onDone)
            }
        }, showLoader = false, showDialog = false, doErrorActionOnNoNetwork = true))
    }
}


private fun PaymentReceiptFragment.continuePolling(
    maxRetriesHere: Int,
    now: Long,
    transactionId: String,
    closePaymentResponse: ClosePaymentResponse,
    onDone: (Boolean) -> Unit
) {
    val retryAfter = closePaymentResponse.retryAfter?.get(0) ?: 30
    val nowHere = Date().time / 1000
    if (maxRetriesHere == 0) {
        dismissDialogWaiting()
        onDone.invoke(false)
    } else {
        showDialogWaiting()
        viewModel.vmDelay(retryAfter - (nowHere.toInt() - now.toInt())) {
            doPolling(
                transactionId,
                closePaymentResponse,
                maxRetriesHere,
                onDone
            )
        }
    }
}

private fun PaymentReceiptFragment.doPolling(
    transactionId: String,
    closePaymentResponse: ClosePaymentResponse,
    maxRetries: Int,
    onDone: (Boolean) -> Unit
) {
    var maxRetriesHere = maxRetries
    val now = Date().time / 1000
    accessTokenLambda { accessToken ->
        viewModel.closePaymentPolling(
            mainActivity!!,
            accessToken,
            closePaymentResponse.location?.get(0).orEmpty()
        ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, {
            dismissDialogWaiting()
            it?.let { mResponse ->
                when (mResponse.status) {
                    Status.CLOSED.status -> onDone.invoke(true)
                    Status.ERROR_ON_CLOSE.status, Status.ERROR_ON_RESULT.status -> goToTechnicalError()
                    else -> {
                        maxRetriesHere--
                        continuePolling(
                            maxRetriesHere,
                            now,
                            transactionId,
                            closePaymentResponse,
                            onDone
                        )
                    }
                }
            } ?: run {
                maxRetriesHere--
                continuePolling(maxRetriesHere, now, transactionId, closePaymentResponse, onDone)
            }
        }, {
            if (it == BaseWrapper.tokenRefreshed)
                doPolling(transactionId, closePaymentResponse, maxRetriesHere, onDone)
            else {
                if (it != BaseWrapper.NO_NETWORK)
                    maxRetriesHere--
                continuePolling(maxRetriesHere, now, transactionId, closePaymentResponse, onDone)
            }
        }, showLoader = false, showDialog = false, doErrorActionOnNoNetwork = true))
    }
}


private fun PaymentReceiptFragment.dismissDialogWaiting() {
    if (dialogWaiting.isVisible) dialogWaiting.dismiss()
}

private fun PaymentReceiptFragment.showDialogWaiting() {
    if (!dialogWaiting.isVisible) {
        dialogWaiting.loading(true)
        dialogWaiting.showDialog(mainActivity?.supportFragmentManager)
    }
}


