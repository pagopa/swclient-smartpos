package it.pagopa.swc.smartpos.idpay.view.cie_read_or_qr_code

import ReadCIE
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.readcie.NisAuthenticated
import it.pagopa.swc.smartpos.app_shared.BaseReadCie
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.CieReaderBinding
import it.pagopa.swc.smartpos.idpay.model.QrCodePoll
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.request.VerifyCieRequest
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.second_screen.showFocusQrSecondScreenWithCode
import it.pagopa.swc.smartpos.idpay.second_screen.showInsertCieSecondScreen
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.DialogTroubleWithQr
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.idPayOpNavigateSuccess
import it.pagopa.swc.smartpos.idpay.view_model.CieReaderOrQrCodeViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.sharedutils.nis.Beep
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import it.pagopa.swc_smartpos.ui_kit.utils.invisibleWithAccessibility
import it.pagopa.swc_smartpos.ui_kit.utils.visibleWithAccessibility
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class CieReaderOrQrCodeFragment : BaseDataBindingFragmentApp<CieReaderBinding>() {
    private var readCie: ReadCIE? = null
    val viewModel: CieReaderOrQrCodeViewModel by viewModels()
    override val layoutId: Int get() = R.layout.cie_reader
    override fun viewBinding() = binding(CieReaderBinding::inflate)
    override val backPress: () -> Unit = {
        mainActivity?.viewModel?.setModel(SaleModel(amount = 0L))
        findNavController().navigateUp()
    }

    override val header: HeaderView = HeaderView(
        HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
            this.backToIntroFragment()
        },
        it.pagopa.swc.smartpos.app_shared.R.color.grey_light
    )
    var dialogTroubleQrCode: DialogTroubleWithQr? = null
    @get:VisibleForTesting
    val areYouSureDialog by lazy {
        UiKitDialog
            .withTitle(getTextSafely(R.string.cancel_id_pay_bonus_are_you_sure_title))
            .withDescription(getTextSafely(R.string.cancel_id_pay_bonus_are_you_sure_description))
            .withMainBtn(getTextSafely(R.string.cancel_id_pay_bonus_are_you_sure_cta_no)) {
                this.idPayOpNavigateSuccess(
                    mainActivity?.viewModel?.model?.value?.availableSale?.toAmountFormatted()
                        .orEmpty()
                )
            }
            .withSecondaryBtn(getTextSafely(R.string.cancel_id_pay_bonus_are_you_sure_cta_yes)) {
                deleteTransaction()
            }
    }
    var waitingDialog: UiKitStyledDialog? = null

    override fun setupListeners() {
        binding.layoutScanQr.btnTroubleWithQr.setOnClickListener {
            dialogTroubleQrCode =
                DialogTroubleWithQr.newInstance(viewModel.uiModel.value.qrCode?.qrCodeFallback) {
                    mainActivity?.showFocusQrSecondScreenWithCode(this.makeQrCodeWithLogo())
                }
            dialogTroubleQrCode?.show(mainActivity?.supportFragmentManager)
            dialogTroubleQrCode?.isCancelable = false
        }
    }

    private fun deleteTransaction() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.cancel_op_loader))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.deleteTransaction(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                viewModel.uiModel.value.transactionId.orEmpty()
            ).observe(viewLifecycleOwner, BaseWrapper(mainActivity!!,
                successAction = {
                    mainActivity?.viewModel?.setModel(SaleModel(isCanceledOp = true))
                    findNavController().navigate(R.id.action_global_resultFragment, bundleOf(
                        BaseResultFragment.stateArg to BaseResultFragment.State.Success,
                        BaseResultFragment.titleArg to R.string.operation_canceled,
                        BaseResultFragment.descriptionArg to R.string.operation_canceled_descr,
                        BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                            getTextSafely(R.string.cta_continue),
                            RUiKit.drawable.arrow_right,
                            false
                        ) {
                            it.findNavController()
                                .navigate(R.id.action_resultFragment_to_receiptFragment)
                        }
                    ))
                }, errorAction = {
                    if (it == BaseWrapper.tokenRefreshed)
                        deleteTransaction()
                })
            )
        }
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        viewModel.setUiModel(arguments?.getSerializableExtra(uiModelCieReader, UiModel::class.java))
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.viewModel?.setKeepScreenOn(true)
        val isNewTransactionId =
            mainActivity?.viewModel?.checkKeyBackStackWithPair<String>("newTransactionId")
        if (isNewTransactionId == true) {
            mainActivity?.viewModel?.getKeyBackStack<Pair<String, String>>()?.let {
                val (key, value) = it
                if (key == "newTransactionId")
                    viewModel.uiModel.value.transactionId = value
            }
        }
        val newUiModel = mainActivity?.viewModel?.checkKeyBackStackWithPair<UiModel>("newUiModel")
        if (newUiModel == true) {
            mainActivity?.viewModel?.getKeyBackStack<Pair<String, UiModel>>()?.let {
                val (key, value) = it
                if (key == "newUiModel")
                    viewModel.setUiModel(value)
            }
        }
        binding.cost.text =
            mainActivity?.viewModel?.model?.value?.amount?.toAmountFormatted().orEmpty()
        val isQr = viewModel.uiModel.value.isQrCode == true
        binding.layoutScanQr.root.isVisible = isQr
        binding.llCie.root.isVisible = !isQr
        if (isQr) {
            mainActivity?.showFocusQrSecondScreenWithCode(this.makeQrCodeWithLogo())
            binding.layoutScanQr inflate this
            val uiModelValue = viewModel.uiModel.value
            this.pollCitizenChoice(
                uiModelValue.maxRetries ?: 3,
                uiModelValue.retryAfter ?: 30,
                uiModelValue.transactionId, action = { status, _, _ ->
                    viewModel.setCitizenSituation(status)
                }, actionAfterStop = {
                    if (it == TransactionStatus.AUTHORIZED.name) {
                        areYouSureDialog.showDialog(mainActivity?.supportFragmentManager)
                    } else {
                        findNavController().popBackStack(R.id.chooseImportFragment, false)
                    }
                })
        } else {
            if (mainActivity?.viewModel?.hasSecondScreen?.value == true)
                binding.llCie.flBallsCie.invisibleWithAccessibility()
            else
                binding.llCie.flBallsCie.visibleWithAccessibility()
            readCie()
        }
    }

    fun readCie() {
        mainActivity?.showInsertCieSecondScreen("WAITING_CARD")
        readCie = ReadCIE(mainActivity!!, viewModel.uiModel.value.challenge)
        readCie?.read(viewModel.viewModelScope, object : BaseReadCie.ReadingCieInterface {
            override fun onTransmit(value: Boolean) {
                mainActivity?.viewModel?.setTransmitting(value)
            }

            override fun backResource(action: BaseReadCie.FunInterfaceResource<NisAuthenticated?>) {
                mainActivity?.viewModel?.setNisAuthenticated(action)
            }
        })
    }

    override fun setupObservers() {
        mainActivity?.viewModel?.setTransmitting(false)
        mainActivity?.viewModel?.setNisAuthenticated(null)
        mainActivity?.viewModel?.nisAuthenticated?.observe(viewLifecycleOwner) {
            viewModel.setNisAuthenticated(it)
        }
        viewModel.nisAuthenticated.observe(viewLifecycleOwner) { funInterfaceResource ->
            if (funInterfaceResource?.status == BaseReadCie.FunInterfaceStatus.SUCCESS) {
                funInterfaceResource.data?.let { nisAuthenticated ->
                    mainActivity?.showInsertCieSecondScreen("READ")
                    viewModel.setCieRead(
                        VerifyCieRequest(
                            nisAuthenticated.nis,
                            nisAuthenticated.kpubIntServ,
                            nisAuthenticated.sod,
                            nisAuthenticated.challengeSigned
                        )
                    )
                    Beep(mainActivity).playSound()
                    val uiModel = viewModel.uiModel.value
                    val ts = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    ).parse(uiModel.timestamp.orEmpty())
                    WrapperLogger.i("Timestamp", "${ts?.time}")
                    this.manageCieView(true, uiModel.euroCents, ts?.time ?: 0L)
                } ?: run {
                    errorReading()
                }
            }
            if (funInterfaceResource?.status == BaseReadCie.FunInterfaceStatus.ERROR)
                errorReading()
        }
        mainActivity?.viewModel?.transmitting?.observe(viewLifecycleOwner) {
            viewModel.setTransmitting(it)
        }
        viewModel.transmitting.observe(viewLifecycleOwner) {
            if (it) {
                mainActivity?.showInsertCieSecondScreen("TRANSMITTING")
                WrapperLogger.i("Transmitting", "$it")
                binding.llCie inflateToThree this
            }
        }
        viewModel.viewModelScope.launch {
            viewModel.citizenSituation.collectLatest {
                this@CieReaderOrQrCodeFragment.manageCitizenSituation(it)
            }
        }
    }

    private fun errorReading() {
        mainActivity?.showInsertCieSecondScreen("ERROR")
        Beep(mainActivity).playErrorSound()
        val uiModel = viewModel.uiModel.value
        val ts = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).parse(uiModel.timestamp.orEmpty())
        this.manageCieView(false, uiModel.euroCents, ts?.time ?: 0L) {
            readCie()
        }
    }

    override fun setupDestroyView() {
        super.setupDestroyView()
        readCie?.disconnect()
        viewModel.void()
        mainActivity?.viewModel?.setKeepScreenOn(false)
        mainActivity?.viewModel?.setTransmitting(false)
        mainActivity?.viewModel?.setNisAuthenticated(null)
    }

    data class UiModel(
        val challenge: String? = null,
        val qrCode: QrCodePoll? = null,
        var transactionId: String? = null,
        val timestamp: String? = null,
        val euroCents: Long? = null,
        val maxRetries: Int? = null,
        val retryAfter: Int? = null,
        val isQrCode: Boolean? = null
    ) : Serializable

    companion object {
        const val uiModelCieReader = "uiModelCieReader"
    }
}