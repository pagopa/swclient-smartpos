package it.pagopa.swc_smartpos.view

import android.Manifest
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.app_shared.permission.PermissionHandler
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.model.presets.PresetOperationsList
import it.pagopa.swc_smartpos.network.NetworkWrapper
import it.pagopa.swc_smartpos.second_screen.showSecondScreenIntro
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseIntroFragment
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.view.utils.ActivateAndReqFeeFragment
import it.pagopa.swc_smartpos.view.utils.callActivate
import it.pagopa.swc_smartpos.view.utils.requestFee
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view_model.ActivateAndRequestFeeBaseVm
import it.pagopa.swc_smartpos.view_model.IntroViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IntroFragment : BaseIntroFragment<MainActivity>(), ActivateAndReqFeeFragment {
    private val viewModel: IntroViewModel by viewModels()
    override fun viewModel(): ActivateAndRequestFeeBaseVm {
        return viewModel
    }

    private val dialogWait by lazy {
        UiKitStyledDialog
            .withStyle(Style.Info)
            .withTitle(getTextSafely(R.string.verifying_advice_title))
            .withDescription(getTextSafely(R.string.verifying_advice_description))
    }

    override val logoClick: () -> Unit = { mainActivity?.showMenuBottomSheet() }
    override val backPress: () -> Unit
        get() = {
            UiKitDialog
                .withTitle(getTextSafely(R.string.title_exitAppDialog))
                .withMainBtn(getTextSafely(R.string.cta_cancel))
                .withSecondaryBtn(getTextSafely(R.string.cta_exitApp)) {
                    mainActivity?.finishAndRemoveTask()
                }
                .showDialog(mainActivity?.supportFragmentManager)
        }
    override val image: Int get() = R.drawable.rata_unica
    override val mainText: Int get() = R.string.title_payNotice
    override val mainBtnText: Int get() = R.string.cta_scanQrCode
    override val mainBtnAction: () -> Unit
        get() = {
            if (PermissionHandler().isPermissionGranted(mainActivity, Manifest.permission.CAMERA))
                findNavController().navigate(R.id.action_introFragment_to_scanCodeFragment)
            else
                findNavController().navigate(R.id.action_introFragment_to_allowCameraAccessFragment)
        }
    override val questionText: Int get() = R.string.label_or
    override val secondaryBtnText: Int get() = R.string.cta_enterManually
    override val secondaryBtnAction: () -> Unit
        get() = {
            findNavController().navigate(R.id.action_introFragment_to_insertManuallyFragment)
        }

    override fun setupOnCreate() {
        super.setupOnCreate()
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    override fun setupUI() {
        super.setupUI()
        viewModel.setCurrentBusiness(mainActivity?.sdkUtils?.getCurrentBusiness()?.value)
        if (mainActivity?.isPoynt != true)
            mainActivity?.showSecondScreenIntro()
    }

    fun dismissDialogWaiting() {
        if (dialogWait.isVisible) dialogWait.dismiss()
    }

    private fun PresetOperationsList.Preset?.elaborateScreen() {
        if (this == null)
            screenWithoutPreset()
        else
            screenWithPreset(this)
    }

    private fun screenWithoutPreset() {
        binding.mainBtn.isVisible = true
        binding.mainBtn.text = getTextSafely(R.string.cta_retry)
        binding.mainBtn.setOnClickListener {
            viewModel.subscriberId.value?.peekContent()?.getLastOpToPay()
        }
        binding.secondaryBtn.isVisible = false
        binding.questionText.isVisible = true
        binding.questionText.text = getTextWithArgs(
            R.string.no_advices_found,
            viewModel.subscriberId.value?.peekContent().orEmpty()
        )
    }

    private fun screenWithPreset(preset: PresetOperationsList.Preset) {
        binding.mainBtn.isVisible = true
        binding.mainBtn.text = getTextSafely(R.string.go_to_last_advice)
        binding.secondaryBtn.isVisible = false
        binding.questionText.isVisible = false
        binding.mainBtn.setOnClickListener {
            verifyPayment(preset)
        }
        verifyPayment(preset)
    }

    private fun verifyPayment(preset: PresetOperationsList.Preset) {
        mainActivity?.let { act ->
            dialogWait.loading(true)
            if (!dialogWait.isAdded) dialogWait.showDialog(act.supportFragmentManager)
            accessTokenLambda { accessToken ->
                act.sdkUtils?.getCurrentBusiness()?.value?.let { business ->
                    viewModel.verify(
                        requireContext(),
                        bearer = accessToken,
                        noticeNumber = preset.noticeNumber,
                        paTaxCode = preset.noticeTaxCode,
                        currentBusiness = business
                    ).observe(viewLifecycleOwner, NetworkWrapper(act, { qrCode ->
                        viewModel.setQrCodeVerifyModel(qrCode)
                        mainActivity?.viewModel?.setReceiptModel(
                            ReceiptModel(
                                labelPayee = qrCode?.company,
                                labelPayeeTaxCode = qrCode?.paTaxCode,
                                labelNoticeCode = qrCode?.noticeNumber?.chunked(4)
                                    ?.joinToString(" "),
                                labelPaymentReason = qrCode?.description
                            )
                        )
                        this.callActivate {
                            viewModel.setPreCloseRequest(
                                PreCloseRequest.PresetRequest(
                                    presetId = preset.presetId,
                                    paTaxCode = preset.paTaxCode,
                                    subcriberId = preset.subscriberId
                                )
                            )
                            this.requestFee(it)
                        }
                    }, {
                        if (it == BaseWrapper.tokenRefreshed)
                            verifyPayment(preset)
                        else
                            dismissDialogWaiting()
                    }, showLoader = false, showSecondScreenLoader = false, isForQrCode = true))
                }
            }
        }
    }

    private fun normalScreen() {
        binding.mainBtn.isVisible = true
        binding.questionText.isVisible = true
        binding.questionText.text = getTextSafely(R.string.label_or)
        binding.secondaryBtn.isVisible = true
        binding.mainBtn.text = getTextSafely(R.string.cta_scanQrCode)
        binding.mainBtn.setOnClickListener {
            mainBtnAction.invoke()
        }
    }

    override fun setupObservers() {
        lifecycleScope.launch {
            mainActivity?.viewModel?.subscriberId?.collectLatest {
                if (viewModel.subscriberId.value?.peekContent() != it)
                    viewModel.setSubscriberId(it)
            }
        }
        lifecycleScope.launch {
            mainActivity?.viewModel?.deactivateHelpedWay?.collectLatest {
                if (it)
                    viewModel.setSubscriberId("")
            }
        }
        viewModel.subscriberId.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it.isNotEmpty()) {
                    binding.questionText.isVisible = false
                    binding.secondaryBtn.isVisible = false
                    it.getLastOpToPay()
                } else
                    normalScreen()
            }
        }
    }

    private fun String.getLastOpToPay() {
        mainActivity.getLastOperation(this, success = { preset ->
            preset.elaborateScreen()
        }, error = {
            screenWithoutPreset()
        })
    }

    private fun MainActivity?.getLastOperation(
        subscriberId: String,
        success: (PresetOperationsList.Preset?) -> Unit,
        error: () -> Unit
    ) {
        if (this == null) return
        this.viewModel.setLoaderText(this.resources.getString(R.string.getting_last_advice))
        val business = this.sdkUtils?.getCurrentBusiness()?.value
        this.viewModel.showLoader(true to false)
        accessTokenLambda { bearer ->
            this@IntroFragment.viewModel.getLastOperation(
                this,
                bearer,
                business,
                subscriberId
            ).observe(viewLifecycleOwner, BaseWrapper(this, successAction = {
                success.invoke(it)
            }, errorAction = {
                if (it == BaseWrapper.tokenRefreshed)
                    getLastOperation(subscriberId, success, error)
                else
                    error.invoke()
            }, showSecondScreenLoader = false,
                showDialog = false,
                doErrorActionOnNoNetwork = true// error 404 means no operation found
            )
            )
        }
    }
}