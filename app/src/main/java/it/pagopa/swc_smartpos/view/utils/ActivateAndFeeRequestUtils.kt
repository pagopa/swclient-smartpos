package it.pagopa.swc_smartpos.view.utils

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentRequest
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentResponse
import it.pagopa.swc_smartpos.model.fee.RequestFeeRequest
import it.pagopa.swc_smartpos.network.NetworkWrapper
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.view.IntroFragment
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import it.pagopa.swc_smartpos.view.PaymentResumeFragment
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view.view_shared.errorDialogAndClosePayment
import it.pagopa.swc_smartpos.view_model.ActivateAndRequestFeeBaseVm
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel

interface ActivateAndReqFeeFragment {
    fun viewModel(): ActivateAndRequestFeeBaseVm
}

fun BaseDataBindingFragment<*, MainActivity>.callActivate(onDone: (ActivatePaymentResponse?) -> Unit) {
    val baseVm = (this as? ActivateAndReqFeeFragment)?.viewModel() ?: return
    baseVm.qrCodeVerify.value?.let { qrCode ->
        if (baseVm.toGenerateTransactionId.value)
            mainActivity?.viewModel?.generateTransactionId()
        baseVm.setToGenerateTransactionId(false)
        baseVm.generateIdempotencyKey { idemPotencyKey ->
            this.accessTokenLambda { accessToken ->
                mainActivity?.viewModel?.setReceiptModel(
                    ReceiptModel(
                        transactionID = mainActivity?.viewModel?.transactionId?.value,
                        labelTerminalCode = mainActivity?.sdkUtils?.getCurrentBusiness()?.value?.terminalId
                    )
                )
                if ((this is PaymentResumeFragment && viewModel.fromManually.value) || this is IntroFragment) {
                    baseVm.activateManualPayment(
                        mainActivity!!,
                        qrCode.paTaxCode, qrCode.noticeNumber, accessToken,
                        ActivatePaymentRequest(
                            idemPotencyKey,
                            qrCode.amount
                        )
                    ).observe(viewLifecycleOwner, NetworkWrapper(mainActivity!!, {
                        onDone.invoke(it)
                    }, {
                        this.toActivateResponseManagementWithAction(it, onDone)
                    }, showLoader = false, isForQrCode = true, doErrorActionOnNoNetwork = true))
                } else {
                    baseVm.activatePayment(
                        mainActivity!!,
                        qrCode.originalCode,
                        accessToken,
                        request = ActivatePaymentRequest(
                            idemPotencyKey,
                            qrCode.amount
                        )
                    ).observe(viewLifecycleOwner, NetworkWrapper(mainActivity!!, {
                        onDone.invoke(it)
                    }, {
                        this.toActivateResponseManagementWithAction(it, onDone)
                    }, showLoader = false, isForQrCode = true, doErrorActionOnNoNetwork = true))
                }
            }
        }
    }
}

private fun BaseDataBindingFragment<*, MainActivity>.toActivateResponseManagementWithAction(
    error: Int?,
    onDone: (ActivatePaymentResponse?) -> Unit
) {
    if (error == BaseWrapper.tokenRefreshed)
        callActivate(onDone)
    else {
        when (this) {
            is PaymentResumeFragment -> binding.btnPay.showLoading(false)
            is IntroFragment -> this.dismissDialogWaiting()
        }
    }
}

fun BaseDataBindingFragment<*, MainActivity>.createPaymentTokens(response: ActivatePaymentResponse?) {
    val viewModel = (this as? ActivateAndReqFeeFragment)?.viewModel() ?: return
    if (viewModel.paymentTokens.value == null)
        viewModel.setPaymentTokens(listOf(response?.paymentToken.orEmpty()))
    else
        viewModel.addPaymentToken(response?.paymentToken.orEmpty())
}

fun BaseDataBindingFragment<*, MainActivity>.requestFee(response: ActivatePaymentResponse?) {
    val viewModel = (this as? ActivateAndReqFeeFragment)?.viewModel() ?: return
    this.createPaymentTokens(response)
    accessTokenLambda { accessToken ->
        viewModel.requestFee(
            mainActivity!!,
            accessToken,
            RequestFeeRequest(
                listOf(
                    RequestFeeRequest.Notice(
                        response?.amount,
                        response?.paTaxCode,
                        response?.transfers
                    )
                )
            )
        ).observe(viewLifecycleOwner, BaseWrapper(mainActivity!!, {
            if (this is PaymentResumeFragment)
                binding.btnPay.showLoading(false)
            if (it?.fee != null) {
                mainActivity?.viewModel?.setReceiptModel(ReceiptModel(labelFee = it.fee.toAmountFormatted()))
                if (this is IntroFragment)
                    this.dismissDialogWaiting()
                findNavController().navigate(
                    if (this is PaymentResumeFragment)
                        R.id.action_paymentResumeFragment_to_paymentReceiptFragment
                    else
                        R.id.action_introFragment_to_paymentReceiptFragment, bundleOf(
                        PaymentReceiptFragment.amountArg to (response?.amount ?: 0),
                        PaymentReceiptFragment.feeArg to it.fee,
                        PaymentReceiptFragment.paymentObjectArg to viewModel.qrCodeVerify.value?.description.orEmpty(),
                        PaymentReceiptFragment.paymentTokens to viewModel.paymentTokens.value?.toTypedArray(),
                        PaymentReceiptFragment.presetNotice to viewModel.preCloseRequest.value
                    )
                )
            } else
                this.errorDialogAndClosePayment()
        }, {
            if (it == BaseWrapper.tokenRefreshed)
                requestFee(response)
            else {
                when (this) {
                    is PaymentResumeFragment -> binding.btnPay.showLoading(false)
                    is IntroFragment -> this.dismissDialogWaiting()
                }
                if (it != BaseWrapper.NO_NETWORK)
                    this.errorDialogAndClosePayment()
            }
        }, showLoader = false, showDialog = false, doErrorActionOnNoNetwork = true))
    }
}