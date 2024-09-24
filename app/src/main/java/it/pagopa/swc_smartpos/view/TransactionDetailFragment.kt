package it.pagopa.swc_smartpos.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.TransactionDetailFragmentBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.Transaction
import it.pagopa.swc_smartpos.printer.PrintReceipt
import it.pagopa.swc_smartpos.second_screen.showSecondScreenIntro
import it.pagopa.swc_smartpos.sharedutils.extensions.dateStringToTimestamp
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getColorSafely
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import it.pagopa.swc_smartpos.view.view_shared.elevateFrameLayoutIfNeeded
import it.pagopa.swc_smartpos.view.view_shared.print
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import it.pagopa.swc_smart_pos.ui_kit.R as RuiKit


class TransactionDetailFragment : BaseDataBindingFragmentApp<TransactionDetailFragmentBinding>() {
    override fun viewBinding() = binding(TransactionDetailFragmentBinding::inflate)
    override val backPress: () -> Unit
        get() = { findNavController().popBackStack() }
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(R.drawable.arrow_back_primary) { findNavController().popBackStack() },
            null,
            HeaderView.HeaderElement(RuiKit.drawable.home_primary) { backToIntroFragment() },
            R.color.white
        )

    private var transaction: Transaction? = null

    private val dialog by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.title_generateReceipt)).withCloseVisible()
            .withMainCustomBtn(CustomBtnCustomizer(getTextSafely(R.string.cta_sendEmail), R.drawable.mail_white, true, this) {

            })
            .withSecondaryCustomBtn(CustomBtnCustomizer(getTextSafely(R.string.cta_printReceipt), R.drawable.print_primary, true, this) {
                mainActivity?.print(receiptDrawable(), false) {
                    it.findNavController().navigate(R.id.action_transactionDetailFragment_to_outroFragment, Bundle().apply {
                        putBoolean(OutroFragment.fromDetail, true)
                    })
                }
            })
    }

    private fun receiptDrawable(): Pair<Drawable?, Int> {
        val instance = PrintReceipt(mainActivity)
        val drawable = instance.printReceipt(transaction.toReceiptModel(), transaction?.getStato())
        return drawable to instance.receiptHeight
    }

    private fun Transaction?.toReceiptModel(): ReceiptModel? {
        if (this == null) return null
        val notice = this.notices?.get(0)
        return ReceiptModel(
            state = Status.valueOf(this.status).state,
            labelDateAndTime = this.transactionTimeString(),
            labelPayee = notice?.company.orEmpty(),
            labelPayeeTaxCode = notice?.paTaxCode.orEmpty(),
            labelNoticeCode = notice?.noticeNumber?.chunked(4)?.joinToString(" ").orEmpty(),
            labelPaymentReason = notice?.description.orEmpty(),
            labelAmount = this.totalAmount?.toAmountFormatted(),
            labelFee = this.fee?.toAmountFormatted(),
            labelTotalAmount = this.amountPlusFee()?.toAmountFormatted(),
            transactionID = this.transactionId,
            labelTerminalCode = mainActivity?.sdkUtils?.getCurrentBusiness()?.value?.terminalId
        )
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        transaction = arguments?.getSerializableExtra("transaction", Transaction::class.java)
    }

    private fun Transaction?.transactionTimeString() = this?.insertTimestamp?.dateStringToTimestamp()?.let { Date(it) }?.let {
        SimpleDateFormat("dd MMM yyyy',' HH:mm", Locale.getDefault()).format(
            it
        ).uppercase()
    }

    override fun setupUI() {
        mainActivity?.showSecondScreenIntro()
        binding.codiceAvviso.text = transaction?.notices?.get(0)?.noticeNumber ?: ""
        binding.totalAmount.text = transaction?.amountPlusFee()?.toAmountFormatted()
        binding.amount.text = transaction?.notices?.get(0)?.amount?.toAmountFormatted()
        binding.timestamp.text = transaction.transactionTimeString()
        binding.fee.text = transaction?.fee?.toAmountFormatted()
        transaction?.getStato()?.let { bindBadgeStatus(it) }
        binding.btnRicevuta.setOnClickListener {
            dialog.showDialog(activity?.supportFragmentManager)
        }
        binding.llDetail.elevateFrameLayoutIfNeeded(binding.nsvDetail, binding.flDoReceipt)
    }

    private fun bindBadgeStatus(status: Status) {
        when (status) {
            Status.CLOSED -> {
                binding.badgeStatus.background =
                    AppCompatResources.getDrawable(requireContext(), RuiKit.drawable.badge_eseguita)
                context.getColorSafely(RuiKit.color.success_graphic)
                    ?.let { binding.badgeStatus.setTextColor(it) }
                binding.badgeStatus.text = getTextSafely(R.string.label_TransactionState_Performed)
            }

            Status.ERROR_ON_CLOSE, Status.ERROR_ON_RESULT -> {
                binding.badgeStatus.background = AppCompatResources.getDrawable(
                    requireContext(),
                    RuiKit.drawable.badge_da_rimborsare
                )
                context.getColorSafely(RuiKit.color.warning_dark)
                    ?.let { binding.badgeStatus.setTextColor(it) }
                binding.badgeStatus.text = getTextSafely(R.string.label_TransactionState_ToBeWriteOff)

            }

            Status.PENDING, Status.PRE_CLOSE -> {
                binding.badgeStatus.background = AppCompatResources.getDrawable(
                    requireContext(),
                    RuiKit.drawable.badge_in_sopseso
                )
                context.getColorSafely(RuiKit.color.info_dark)
                    ?.let { binding.badgeStatus.setTextColor(it) }
                binding.badgeStatus.text = getTextSafely(RuiKit.string.label_transactionState_pending)
            }

            Status.ABORT, Status.ERROR_ON_PAYMENT -> {
                binding.badgeStatus.background =
                    AppCompatResources.getDrawable(requireContext(), RuiKit.drawable.badge_fallita)
                context.getColorSafely(RuiKit.color.error_dark)
                    ?.let { binding.badgeStatus.setTextColor(it) }
                binding.badgeStatus.text = getTextSafely(R.string.label_transactionState_failed)
                binding.flDoReceipt.isVisible = false
            }

            Status.RIMBORSATA -> {
                binding.badgeStatus.background = AppCompatResources.getDrawable(
                    requireContext(),
                    RuiKit.drawable.badge_rimborsata
                )
                context.getColorSafely(RuiKit.color.blue_io_dark)
                    ?.let { binding.badgeStatus.setTextColor(it) }
                binding.badgeStatus.text = getTextSafely(R.string.label_TransactionState_WriteOff)
                binding.flDoReceipt.isVisible = false
            }
        }
    }
}