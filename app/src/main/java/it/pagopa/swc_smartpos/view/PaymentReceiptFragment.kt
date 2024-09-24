package it.pagopa.swc_smartpos.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.PaymentAmountResumeBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.second_screen.bindSecondScreen
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.orEmptyCharSeq
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.sharedutils.model.PaymentStatus
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import it.pagopa.swc_smartpos.view.utils.callPreClose
import it.pagopa.swc_smartpos.view.utils.doClosePayment
import it.pagopa.swc_smartpos.view.view_shared.doAbortPreClosePayment
import it.pagopa.swc_smartpos.view.view_shared.doClosePaymentKo
import it.pagopa.swc_smartpos.view.view_shared.elevateFrameLayoutIfNeeded
import it.pagopa.swc_smartpos.view_model.PaymentReceiptViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import java.util.*

class PaymentReceiptFragment : BaseDataBindingFragmentApp<PaymentAmountResumeBinding>() {
    val viewModel: PaymentReceiptViewModel by viewModels()
    override val backPress: () -> Unit get() = { }

    val dialog by lazy {
        UiKitStyledDialog
            .withStyle(Style.Info)
            .withTitle(getStringSafely(R.string.title_continueOnPos))
            .withDescription(getStringSafely(R.string.paragraph_continueOnPos))
    }

    val dialogWaiting by lazy {
        UiKitStyledDialog
            .withStyle(Style.Info)
            .withTitle(getStringSafely(R.string.title_payeeUnavailable))
            .withDescription(getStringSafely(R.string.paragraph_payeeUnavailable))
    }

    override fun viewBinding() = binding(PaymentAmountResumeBinding::inflate)
    internal var amount: Model? = null
    private var presetRequest: PreCloseRequest.PresetRequest? = null
    override val header: HeaderView
        get() = HeaderView(
            null,
            null,
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
                UiKitDialog
                    .withTitle(
                        mainActivity?.resources?.getText(R.string.title_exitDialog).orEmptyCharSeq()
                    )
                    .withDescription(
                        mainActivity?.resources?.getText(R.string.paragraph_exitDialog)
                            .orEmptyCharSeq()
                    )
                    .withMainBtn(
                        mainActivity?.resources?.getText(R.string.cta_cancel).orEmptyCharSeq()
                    )
                    .withSecondaryBtn(
                        mainActivity?.resources?.getText(R.string.CTA_AbortPayment).orEmptyCharSeq()
                    ) {
                        this.doAbortPreClosePayment()
                    }
                    .showDialog(mainActivity?.supportFragmentManager)
            },
            R.color.white
        )

    @Suppress("UNCHECKED_CAST")
    override fun setupOnCreate() {
        super.setupOnCreate()
        arguments?.let {
            val value = it.getInt(amountArg)
            val fee = it.getInt(feeArg)
            val paymentObject = it.getString(paymentObjectArg)
            val paymentTokens = it.getSerializableExtra(paymentTokens, Array::class.java)
            presetRequest = it.getSerializableExtra(presetNotice, PreCloseRequest.PresetRequest::class.java)
            amount = Model(
                value,
                fee,
                paymentObject.orEmpty(),
                paymentTokens?.toList() as? List<String>? ?: listOf()
            )
        }
        mainActivity?.viewModel?.setKeepScreenOn(true)
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.btnPayAmount.setOnClickListener {
            viewModel.amount.value?.amount?.let {
                binding.btnPayAmount.showLoading(true)
                this.callPreClose(3, it.toLong(), viewModel.amount.value!!.totalAmount.toLong(), presetRequest)
            }
        }
    }

    override fun setupObservers() {
        mainActivity?.viewModel?.payment?.observe(viewLifecycleOwner) {
            it?.let { payment ->
                WrapperLogger.i("Payment", payment.status.name)
                binding.btnPayAmount.showLoading(false)
                if (dialog.isVisible)
                    dialog.dismiss()
                when (payment.status) {
                    PaymentStatus.CANCELED, PaymentStatus.FAIL_TO_LAUNCH -> doClosePaymentKo()
                    PaymentStatus.COMPLETED -> closePayment()
                    else -> doClosePaymentKo(false)
                }
                mainActivity?.viewModel?.setPayment(null)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.amount.collectLatest {
                it?.let { model ->
                    binding.paymentAmount.text = model.amount.toAmountFormatted()
                    binding.feeAmount.text = model.fee.toAmountFormatted()
                    binding.paymentDescription.text = model.paymentObject
                    val totalValueFormatted = model.totalAmount.toAmountFormatted()
                    binding.totalValue.text = totalValueFormatted
                    context?.resources?.getString(R.string.cta_pay, totalValueFormatted)?.let { string ->
                        binding.btnPayAmount.setButtonText(string)
                    }
                    model.bindSecondScreen(mainActivity)
                    binding.llAmount.elevateFrameLayoutIfNeeded(
                        binding.nsvAmount,
                        binding.flBtnAmount
                    )
                }
            }
        }
    }

    override fun setupUI() {
        mainActivity?.viewModel?.setReceiptModel(
            ReceiptModel(
                labelTotalAmount = amount?.totalAmount?.toAmountFormatted().orEmpty()
            )
        )
        viewModel.setAmount(amount)
        context?.resources?.getString(R.string.label_fee)?.uppercase()?.let {
            binding.feeDescription.text = it
        }
        viewModel.setCurrentBusiness(mainActivity?.sdkUtils?.getCurrentBusiness()?.value)
    }

    private fun closePayment() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.feedback_loading_paymentOutcome))
        mainActivity?.viewModel?.showLoader(true to true)
        viewModel.amount.value?.let { mModel ->
            doClosePayment(mModel)
        }
    }

    data class Model(
        val amount: Int,
        val fee: Int,
        val paymentObject: String,
        val paymentTokens: List<String>
    ) : java.io.Serializable {
        val totalAmount = amount + fee
    }

    companion object {
        const val paymentObjectArg = "paymentObjectArg"
        const val amountArg = "amountArg"
        const val feeArg = "feeArg"
        const val paymentTokens = "paymentTokens"
        const val presetNotice = "isPresetNotice"
    }
}