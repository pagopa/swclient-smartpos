package it.pagopa.swc.smartpos.idpay.view.cie_read_or_qr_code

import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.MainViewModel
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.CardScanQrFromIoBinding
import it.pagopa.swc.smartpos.idpay.databinding.ReadCieLayoutBinding
import it.pagopa.swc.smartpos.idpay.model.QrCodePoll
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.response.TransactionDetailResponse
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import it.pagopa.swc.smartpos.idpay.network.NetworkErrorMessageWrapper
import it.pagopa.swc.smartpos.idpay.second_screen.showWaitCitizenDecisionSecondScreen
import it.pagopa.swc.smartpos.idpay.view.ConfirmCieOperation
import it.pagopa.swc.smartpos.idpay.view.ResultFragment
import it.pagopa.swc.smartpos.idpay.view.view_shared.acceptNewBonus
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.genericErrorDialog
import it.pagopa.swc.smartpos.idpay.view.view_shared.idPayOpNavigateSuccess
import it.pagopa.swc.smartpos.idpay.view_model.CieReaderOrQrCodeViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.sharedutils.qrCode.generate_qr.QrCodeUtils
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import it.pagopa.swc.smartpos.app_shared.R as RShared

//CIE CASE
fun CieReaderOrQrCodeFragment.manageCieView(
    itsOk: Boolean,
    euroCents: Long?,
    epochMillis: Long?,
    onAnimEnd: (() -> Unit)? = null
) {
    if (!this.isVisible) return
    mainActivity?.runOnUiThread {
        binding.llCie.cieCvTitle.text =
            getTextSafely(if (itsOk) R.string.cie_read_description else R.string.cie_failed_read_description)
        binding.llCie.mainImage.setImageResource(if (itsOk) R.drawable.card_read else R.drawable.read_card_error)
    }
    binding.llCie.animateReadCie(this, itsOk) {
        if (itsOk)
            callVerifyCie(euroCents, epochMillis)
        else {
            onAnimEnd?.invoke()
            binding.llCie backToStart this
        }
    }
}

private fun CieReaderOrQrCodeFragment.callVerifyCie(
    euroCents: Long?,
    epochMillis: Long?
) {
    fun voidView() {
        this.viewModel.void().also {
            mainActivity?.viewModel?.setTransmitting(false)
            mainActivity?.viewModel?.setNisAuthenticated(null)
            binding.llCie backToStart this
            this.readCie()
        }
        mainActivity?.viewModel?.showLoader(false to false)
    }
    mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.reading_cie))
    mainActivity?.viewModel?.showLoader(true to true)
    accessTokenLambda { bearer ->
        viewModel.verifyCie(
            mainActivity!!,
            bearer,
            mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
            request = viewModel.cieRead.value,
            transactionId = viewModel.uiModel.value.transactionId.orEmpty()
        ).observe(
            this.mainActivity!!, BaseWrapper(mainActivity, successAction = {
                it?.let { response ->
                    val uiModel = this.viewModel.uiModel.value
                    mainActivity?.viewModel?.showLoader(false to false)
                    mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.verify_id_pay_portfolio))
                    mainActivity?.viewModel?.showLoader(true to true)
                    this.pollCitizenChoice(
                        uiModel.maxRetries ?: 3,
                        uiModel.retryAfter ?: 30,
                        uiModel.transactionId,
                        action = { status, coveredAmount, secondFactor ->
                            if (status == TransactionStatus.IDENTIFIED.name) {
                                if (coveredAmount == null || coveredAmount == 0L) {
                                    mainActivity?.viewModel?.showLoader(false to false)
                                    this.navigateToErrorResult("retry")
                                } else {
                                    mainActivity?.viewModel?.showLoader(false to false)
                                    findNavController().navigate(
                                        R.id.action_cieReaderFragment_to_confirmCieOperation,
                                        bundleOf(
                                            ConfirmCieOperation.uiModelConfirmCIeOp to ConfirmCieOperation.UiModel(
                                                euroCents = euroCents ?: 0L,
                                                epochMillis = epochMillis ?: 0L,
                                                nis = viewModel.cieRead.value.nis,
                                                response = response,
                                                transactionId = viewModel.uiModel.value.transactionId.orEmpty(),
                                                coveredAmount = coveredAmount,
                                                secondFactor = secondFactor.orEmpty()
                                            )
                                        )
                                    )
                                }
                            } else
                                this@callVerifyCie.manageCitizenSituation(status)
                        }, isForCie = true
                    )
                }
            }, errorAction = {
                when (it) {
                    BaseWrapper.tokenRefreshed -> callVerifyCie(euroCents, epochMillis)
                    BaseWrapper.NO_NETWORK -> voidView()
                    else -> {
                        voidView()
                        this.navigateToErrorResult("Auth with IO")
                    }
                }
            }, showLoader = false, showDialog = false, doErrorActionOnNoNetwork = true)
        )
    }
}

infix fun ReadCieLayoutBinding.inflateToThree(fragment: CieReaderOrQrCodeFragment) {
    fragment.viewModel.viewModelScope.launch {
        (1..2).asSequence().asFlow().onEach {
            delay(250L)
        }.collectLatest {
            when (it) {
                1 -> this@inflateToThree.circleTwo.background =
                    AppCompatResources.getDrawable(
                        fragment.requireContext(),
                        R.drawable.rounded_success_drawable_100_dp
                    )

                2 -> this@inflateToThree.circleThree.background =
                    AppCompatResources.getDrawable(
                        fragment.requireContext(),
                        R.drawable.rounded_success_drawable_100_dp
                    )
            }
        }
    }
}

infix fun ReadCieLayoutBinding.backToStart(fragment: CieReaderOrQrCodeFragment) {
    this.cieCvTitle.text = fragment.getTextSafely(R.string.insert_cie)
    this.mainImage.setImageResource(R.drawable.cie_example_image)
    this.circleOne.background = AppCompatResources.getDrawable(
        fragment.requireContext(),
        R.drawable.rounded_success_drawable_100_dp
    )
    this.circleTwo.background = AppCompatResources.getDrawable(
        fragment.requireContext(),
        R.drawable.rounded_grey_light_drawable_100_dp
    )
    this.circleThree.background = AppCompatResources.getDrawable(
        fragment.requireContext(),
        R.drawable.rounded_grey_light_drawable_100_dp
    )
    this.circleFour.background = AppCompatResources.getDrawable(
        fragment.requireContext(),
        R.drawable.rounded_grey_light_drawable_100_dp
    )
}

fun ReadCieLayoutBinding.animateReadCie(
    fragment: CieReaderOrQrCodeFragment,
    itsOk: Boolean,
    actionDone: () -> Unit
) {
    this@animateReadCie.circleFour.background = AppCompatResources.getDrawable(
        fragment.requireContext(),
        if (itsOk) R.drawable.rounded_success_drawable_100_dp else R.drawable.rounded_error_drawable_100_dp
    )
    fragment.viewModel.viewModelScope.launch {
        delay(1000L)
        actionDone.invoke()
    }
}

private fun TransactionDetailResponse.setMainVmSaleModel(mainVm: MainViewModel?) {
    mainVm?.setModel(
        SaleModel(
            availableSale = this.coveredAmount,
            amount = this.goodsCost,
            percentageSale = ((this.coveredAmount?.toFloat() ?: 0f) / (this.goodsCost?.toFloat()
                ?: 1f)) * 100f
        )
    )
}

fun CieReaderOrQrCodeFragment.pollCitizenChoice(
    maxRetries: Int, retryAfter: Int, transactionId: String?,
    action: (String?, Long?, String?) -> Unit,
    actionAfterStop: ((String?) -> Unit)? = null,
    isForCie: Boolean = false
) {
    if (!this.isAdded) return
    if (maxRetries > 0) {
        accessTokenLambda { bearer ->
            fun poll(maxRetriesPoll: Int) {
                if (maxRetriesPoll > 0) {
                    this.viewModel.idPayTransactionDetail(
                        mainActivity!!,
                        bearer,
                        mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                        transactionId.orEmpty()
                    ).observe(
                        viewLifecycleOwner, NetworkErrorMessageWrapper(
                            mainActivity!!,
                            successAction = { response ->
                                response?.let {
                                    if (this.viewModel.citizenSituation.value == "STOP") {
                                        mainActivity?.viewModel?.showLoader(false to false)
                                        actionAfterStop?.invoke(it.status)
                                    } else {
                                        if (this.viewModel.citizenSituation.value == TransactionStatus.IDENTIFIED.name && it.status == TransactionStatus.CREATED.name) {
                                            it.setMainVmSaleModel(mainActivity?.viewModel)
                                            action.invoke(
                                                it.status,
                                                it.coveredAmount,
                                                it.secondFactor
                                            )
                                        } else {
                                            if (!isForCie) {
                                                if (it.status == TransactionStatus.CREATED.name || it.status == TransactionStatus.IDENTIFIED.name) {
                                                    viewModel.refreshMaxRetriesAndRePoll(
                                                        maxRetriesPoll, retryAfter
                                                    ) { maxRetries -> poll(maxRetries) }
                                                }
                                                it.setMainVmSaleModel(mainActivity?.viewModel)
                                                action.invoke(
                                                    it.status,
                                                    it.coveredAmount,
                                                    it.secondFactor
                                                )
                                            } else {
                                                if (it.status == TransactionStatus.CREATED.name) {
                                                    viewModel.refreshMaxRetriesAndRePoll(
                                                        maxRetriesPoll, retryAfter
                                                    ) { maxRetries -> poll(maxRetries) }
                                                } else
                                                    HttpServiceInterfaceMocked.cntCallForFakePoll =
                                                        0
                                                it.setMainVmSaleModel(mainActivity?.viewModel)
                                                action.invoke(
                                                    it.status,
                                                    it.coveredAmount,
                                                    it.secondFactor
                                                )//HERE STATUS SHOULD BE IDENTIFIED
                                            }
                                        }
                                    }
                                } ?: run {
                                    viewModel.refreshMaxRetriesAndRePoll(
                                        maxRetriesPoll, retryAfter
                                    ) { maxRetries -> poll(maxRetries) }
                                }
                            },
                            errorAction = {
                                when (it) {
                                    BaseWrapper.tokenRefreshed -> pollCitizenChoice(
                                        maxRetriesPoll,
                                        retryAfter,
                                        transactionId,
                                        action,
                                        actionAfterStop
                                    )

                                    BaseWrapper.NO_NETWORK -> viewModel.refreshMaxRetriesAndRePoll(
                                        maxRetriesPoll + 1, retryAfter
                                    ) { maxRetries -> poll(maxRetries) }

                                    else -> {
                                        if (it != 500) {
                                            if (isForCie)
                                                this.navigateToErrorResult("Auth with IO")
                                            else
                                                this.navigateToErrorResult("Poll error Qr Code")
                                        }
                                    }
                                }
                            },
                            error500 = {
                                if (!isForCie) {
                                    this.navigateToErrorResult("Poll error Qr Code")
                                } else {
                                    if (it == "00A000050") {
                                        viewModel.refreshMaxRetriesAndRePoll(
                                            maxRetriesPoll, retryAfter
                                        ) { maxRetries -> poll(maxRetries) }
                                    } else
                                        this.navigateToErrorResult("Auth with IO")
                                }
                            },
                            showLoader = false,
                            showDialog = false,
                            doErrorActionOnNoNetwork = true
                        )
                    )
                } else
                    action.invoke("Too much", null, null)
            }
            if (mainActivity?.getCurrentFragment() is CieReaderOrQrCodeFragment)
                poll(maxRetries)
        }
    } else {
        action.invoke("Too much", null, null)
    }
}

private fun CieReaderOrQrCodeViewModel.refreshMaxRetriesAndRePoll(
    maxRetries: Int,
    retryAfter: Int,
    rePollAction: (Int) -> Unit
) {
    var maxRetriesHere = maxRetries
    maxRetriesHere--
    this.viewModelScope.launch {
        delay((retryAfter * 1000).toLong())
        rePollAction.invoke(maxRetriesHere)
    }
}

private fun ResultFragment.refreshCieOrQrModelAndBack(isQr: Boolean?) {
    this.viewModel.cancelOpAndRecreateNewComplete(this, errorAction = {
        //TODO verify fallback with P&D
        this.mainActivity?.genericErrorDialog()
    }) { createTransactionResponse ->
        val uiModel = CieReaderOrQrCodeFragment.UiModel(
            challenge = createTransactionResponse.challenge,
            qrCode = QrCodePoll(
                createTransactionResponse.qrCode,
                createTransactionResponse.trxCode
            ),
            transactionId = createTransactionResponse.milTransactionId.orEmpty(),
            timestamp = createTransactionResponse.timestamp,
            euroCents = createTransactionResponse.goodsCost,
            maxRetries = createTransactionResponse.maxRetries?.get(0) ?: 3,
            retryAfter = createTransactionResponse.retryAfter?.get(0) ?: 30,
            isQrCode = isQr
        )
        this.mainActivity?.viewModel?.setKeyBackStack("newUiModel" to uiModel)
        this.findNavController().popBackStack(R.id.cieReaderFragment, false)
    }
}

//QR CODE CASE
private fun CieReaderOrQrCodeFragment.navigateToErrorResult(case: String) {
    this.mainActivity?.viewModel?.showLoader(false to false)
    if (waitingDialog?.isVisible == true)
        waitingDialog?.dismiss()
    val isQr = this.viewModel.uiModel.value.isQrCode
    WrapperLogger.i("Case", case)
    val bundle = when (case) {
        "Poll error Qr Code" -> bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Error,
            BaseResultFragment.titleArg to RShared.string.title_unknownError,
            BaseResultFragment.descriptionArg to R.string.no_possible_to_continue_op,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag) {
                    frag.acceptNewBonus(frag.viewModel)
                }
            },
            ResultFragment.operationState to ResultFragment.OperationState.DELETED
        )

        "Auth with IO" -> bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Error,
            BaseResultFragment.titleArg to RShared.string.title_unknownError,
            BaseResultFragment.descriptionArg to R.string.no_possible_to_continue_op,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.authorize_with),
                drawable = R.drawable.logo_io_white,
                isStart = false
            ) {
                (it as ResultFragment).refreshCieOrQrModelAndBack(true)
            }, BaseResultFragment.secondButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag) {
                    frag.acceptNewBonus(frag.viewModel)
                }
            },
            ResultFragment.operationState to ResultFragment.OperationState.DELETED
        )

        "From Identified", TransactionStatus.REJECTED.name -> bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Warning,
            BaseResultFragment.titleArg to if (case == TransactionStatus.REJECTED.name)
                R.string.auth_denied
            else
                R.string.canceled_op_by_io_title,
            BaseResultFragment.descriptionArg to if (case == TransactionStatus.REJECTED.name)
                R.string.auth_denied_description
            else
                R.string.canceled_op_by_io_description,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag) {
                    frag.acceptNewBonus(frag.viewModel)
                }
            }, BaseResultFragment.secondButtonArg to CustomBtnCustomizer(
                getTextSafely(RShared.string.cta_retry)
            ) {
                (it as ResultFragment).refreshCieOrQrModelAndBack(isQr)
            },
            ResultFragment.operationState to ResultFragment.OperationState.DELETED
        )

        "retry" -> bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Info,
            BaseResultFragment.titleArg to R.string.session_expired_by_io_title,
            BaseResultFragment.descriptionArg to R.string.session_expired_by_io_description,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(RShared.string.cta_retry)
            ) {
                (it as ResultFragment).refreshCieOrQrModelAndBack(isQr)
            }, BaseResultFragment.secondButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag) {
                    frag.acceptNewBonus(frag.viewModel)
                }
            },
            ResultFragment.operationState to ResultFragment.OperationState.MAX_RETRIES
        )

        else -> bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Info,
            BaseResultFragment.titleArg to R.string.session_expired_by_io_title,
            BaseResultFragment.descriptionArg to R.string.session_expired_by_io_description,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(RShared.string.cta_retry)
            ) {
                (it as ResultFragment).refreshCieOrQrModelAndBack(isQr)
            }, BaseResultFragment.secondButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag) {
                    frag.acceptNewBonus(frag.viewModel)
                }
            },
            ResultFragment.operationState to ResultFragment.OperationState.MAX_RETRIES
        )
    }
    navigate(R.id.action_global_resultFragment, bundle)
}

fun CieReaderOrQrCodeFragment.manageCitizenSituation(value: String?) {
    WrapperLogger.i("Manage Value", value.orEmpty())
    when (value) {
        TransactionStatus.REJECTED.name, "Too much", "From Identified" -> {
            if (waitingDialog?.isVisible == true)
                waitingDialog?.dismiss()
            this.navigateToErrorResult(
                value
            )
        }

        TransactionStatus.IDENTIFIED.name -> {
            if (waitingDialog == null) {
                waitingDialog = UiKitStyledDialog
                    .withStyle(Style.Info)
                    .withTitle(getTextSafely(R.string.wait_auth))
                    .withDescription(getTextSafely(R.string.wait_auth_description))
                waitingDialog?.withSecondaryBtn(getTextSafely(R.string.cta_cancel)) {
                    if (waitingDialog?.isVisible == true)
                        waitingDialog?.dismiss()
                    viewModel.setCitizenSituation("STOP")
                    mainActivity?.viewModel?.showLoader(true to true)
                    mainActivity?.viewModel?.setLoaderText(getStringSafely(it.pagopa.swc.smartpos.app_shared.R.string.feedback_loading_generic))
                }
                waitingDialog?.loading(true)
            }
            if (waitingDialog?.isVisible != true && waitingDialog?.isAdded != true) {
                mainActivity?.showWaitCitizenDecisionSecondScreen()
                if (dialogTroubleQrCode?.isVisible == true)
                    dialogTroubleQrCode?.dismiss()
                waitingDialog?.showDialog(mainActivity?.supportFragmentManager)
            }
        }

        TransactionStatus.AUTHORIZED.name -> {
            if (waitingDialog?.isVisible == true)
                waitingDialog?.dismiss()
            this.idPayOpNavigateSuccess(
                mainActivity?.viewModel?.model?.value?.availableSale?.toAmountFormatted().orEmpty()
            )
        }
    }
}

fun CieReaderOrQrCodeFragment.makeQrCodeWithLogo(): Bitmap? {
    val logo = if (mainActivity != null) {
        val qrSize =
            mainActivity!! dpToPx mainActivity!!.resources.getInteger(R.integer.qr_code_size_integer)
                .toFloat()
        val logoSize = (qrSize * 0.1f).toInt()
        ContextCompat.getDrawable(mainActivity!!, R.drawable.io_logo)?.toBitmap(logoSize, logoSize)
    } else
        null
    val code = viewModel.uiModel.value.qrCode?.qrCode.orEmpty()
    WrapperLogger.i("QrCode", code)
    QrCodeUtils().textQrCode(code, logo)?.let {
        return it
    } ?: run {
        return null
    }
}

infix fun CardScanQrFromIoBinding.inflate(fragment: CieReaderOrQrCodeFragment) {
    fragment.makeQrCodeWithLogo()?.let {
        this.qrImage.setImageBitmap(it)
    }
}