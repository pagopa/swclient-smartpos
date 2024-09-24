package it.pagopa.swc.smartpos.idpay.view

import ReadCIE
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.ChooseImportBinding
import it.pagopa.swc.smartpos.idpay.model.QrCodePoll
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.model.response.CreateTransactionResponse
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenIntro
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.cie_read_or_qr_code.CieReaderOrQrCodeFragment
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.errorDialog
import it.pagopa.swc.smartpos.idpay.view_model.ChooseImportViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.disablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.enablePrimaryButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChooseImportFragment : BaseDataBindingFragmentApp<ChooseImportBinding>() {
    private val viewModel: ChooseImportViewModel by viewModels()
    private fun String.toLongOrZero() = if (this.isEmpty()) 0L else this.toLong()
    override val backPress: () -> Unit = { findNavController().navigateUp() }
    override val layoutId: Int get() = R.layout.choose_import
    override fun viewBinding() = binding(ChooseImportBinding::inflate)
    override val header: HeaderView = HeaderView(
        HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
            this.backToIntroFragment()
        },
        R.color.white
    )
    private val dialogChooseMethod by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.how_to_authorize_bonus))
            .withMainBtn(getTextSafely(R.string.auth_with_id_card)) {
                navigateAfter(false)
            }.withSecondaryCustomBtn(
                CustomBtnCustomizer(
                    getTextSafely(R.string.auth_with),
                    R.drawable.logo_io, false, this
                ) {
                    navigateAfter(true)
                }
            ).withCloseVisible()
    }

    private fun navigateAfter(isQr: Boolean) {
        findNavController().navigate(
            R.id.action_chooseImportFragment_to_cieReaderFragment,
            bundleOf(
                CieReaderOrQrCodeFragment.uiModelCieReader to CieReaderOrQrCodeFragment.UiModel(
                    challenge = viewModel.transaction.value?.challenge.orEmpty(),
                    qrCode = QrCodePoll(
                        viewModel.transaction.value?.qrCode,
                        viewModel.transaction.value?.trxCode
                    ),
                    transactionId = viewModel.transaction.value?.milTransactionId.orEmpty(),
                    timestamp = viewModel.transaction.value?.timestamp,
                    euroCents = viewModel.transaction.value?.goodsCost,
                    maxRetries = viewModel.transaction.value?.maxRetries?.get(0) ?: 3,
                    retryAfter = viewModel.transaction.value?.retryAfter?.get(0) ?: 30,
                    isQrCode = isQr
                )
            )
        )
    }

    private fun navigateToQrCode() {
        navigateAfter(true)
    }

    override fun setupUI() {
        super.setupUI()
        viewModel.setAmount(0L)
        mainActivity?.showSecondScreenIntro()
        binding.customKeyboard.doAfterTextChanged = {
            val value = it.toLongOrZero()
            WrapperLogger.d("Text", "$value")
            viewModel.setAmount(value)
        }
    }

    override fun setupListeners() {
        binding.btnCalculateSale.setOnClickListener {
            if (!binding.btnCalculateSale.isLoading) {
                binding.btnCalculateSale.showLoading(true)
                createTransaction()
            }
        }
    }

    private fun CreateTransactionResponse.setSaleModel() {
        mainActivity?.viewModel?.setModel(
            SaleModel(
                amount = this.goodsCost,
                milTransactionId = this.milTransactionId,
                timeStamp = this.timestamp
            )
        )
    }

    private fun CreateTransactionResponse?.goOnWithCtrl() {
        binding.btnCalculateSale.showLoading(false)
        if (this == null || this.goodsCost == null) {
            this@ChooseImportFragment.errorDialog()
        } else {
            viewModel.setTransaction(this)
            this.setSaleModel()
            if (ReadCIE.isNfcAvailable(context))
                dialogChooseMethod.showDialog(mainActivity?.supportFragmentManager)
            else
                navigateToQrCode()
        }
    }

    private fun createTransaction() {
        accessTokenLambda { bearer ->
            val saleModel = mainActivity?.viewModel?.model?.value
            viewModel.createTransaction(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                CreateTransactionRequest(
                    initiativeId = saleModel?.initiative?.id.orEmpty(),
                    timestamp = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date()),
                    goodsCost = viewModel.amount.value
                )
            ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, successAction = { response ->
                response?.goOnWithCtrl()
            }, errorAction = {
                when (it) {
                    BaseWrapper.tokenRefreshed -> createTransaction()
                    else -> binding.btnCalculateSale.showLoading(false)
                }
            }, showLoader = false, doErrorActionOnNoNetwork = true))
        }
    }

    override fun setupObservers() {
        viewModel.viewModelScope.launch {
            viewModel.amount.collectLatest {
                if (it > 0L)
                    binding.btnCalculateSale.enablePrimaryButton(mainActivity)
                else
                    binding.btnCalculateSale.disablePrimaryButton(mainActivity)
            }
        }
    }
}