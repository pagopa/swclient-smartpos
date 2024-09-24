package it.pagopa.swc_smartpos.view

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.ItemPaymentBinding
import it.pagopa.swc_smartpos.databinding.PaymentResumeBinding
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.second_screen.bindSecondScreen
import it.pagopa.swc_smartpos.sharedutils.extensions.orEmptyCharSeq
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.disableScroll
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPxWith
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import it.pagopa.swc_smartpos.view.utils.ActivateAndReqFeeFragment
import it.pagopa.swc_smartpos.view.utils.callActivate
import it.pagopa.swc_smartpos.view.utils.requestFee
import it.pagopa.swc_smartpos.view.view_shared.doAbortPreClosePayment
import it.pagopa.swc_smartpos.view_model.ActivateAndRequestFeeBaseVm
import it.pagopa.swc_smartpos.view_model.PaymentResumeViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaymentResumeFragment : BaseDataBindingFragmentApp<PaymentResumeBinding>(), ActivateAndReqFeeFragment {
    val viewModel: PaymentResumeViewModel by viewModels()
    override fun viewModel(): ActivateAndRequestFeeBaseVm {
        return viewModel
    }
    override val backPress: () -> Unit get() = {}
    override val header: HeaderView
        get() = HeaderView(
            null,
            null,
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
                UiKitDialog
                    .withTitle(mainActivity?.resources?.getText(R.string.title_exitDialog).orEmptyCharSeq())
                    .withDescription(mainActivity?.resources?.getText(R.string.paragraph_exitDialog).orEmptyCharSeq())
                    .withMainBtn(mainActivity?.resources?.getText(R.string.cta_cancel).orEmptyCharSeq())
                    .withSecondaryBtn(mainActivity?.resources?.getText(R.string.CTA_AbortPayment).orEmptyCharSeq()) {
                        this.doAbortPreClosePayment()
                    }
                    .showDialog(mainActivity?.supportFragmentManager)
            },
            R.color.white
        )

    override fun viewBinding() = binding(PaymentResumeBinding::inflate)
    override fun setupOnCreate() {
        super.setupOnCreate()
        viewModel.setQrCodeVerifyModel(arguments?.getSerializableExtra(qrCodeParam, QrCodeVerifyResponse::class.java))
        viewModel.setFromManually(arguments?.getBoolean(fromManuallyParam) ?: false)
    }

    override fun setupListeners() {
        binding.btnPay.setOnClickListener {
            if (!binding.btnPay.isLoading) {
                mainActivity?.runOnUiThread {
                    binding.btnPay.showLoading(true)
                }
                clickOnPayLogic()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.qrCodeVerify.collectLatest {
                it?.let { model ->
                    mainActivity?.viewModel?.setReceiptModel(
                        ReceiptModel(
                            labelPayee = model.company,
                            labelPayeeTaxCode = model.paTaxCode,
                            labelNoticeCode = model.noticeNumber.chunked(4).joinToString(" "),
                            labelPaymentReason = model.description
                        )
                    )
                    model.setupRv()
                }
            }
        }
    }

    private fun QrCodeVerifyResponse.setupRv() {
        binding.rvPaymentResume.adapter = Adapter(this.toListItems())
        binding.rvPaymentResume.disableScroll()
        this.bindSecondScreen(mainActivity)
        binding.llResume.post {
            if (binding.llResume.height > binding.nsvResume.height) {
                context?.let { ctx -> binding.flBtn.elevation = 16f dpToPxWith ctx }
            } else
                binding.flBtn.elevation = 0f
        }
    }

    override fun setupUI() {
        viewModel.setCurrentBusiness(mainActivity?.sdkUtils?.getCurrentBusiness()?.value)
        viewModel.setToGenerateTransactionId(true)
    }

    private fun clickOnPayLogic() {
        this.callActivate {
            this.requestFee(it)
        }
    }

    @VisibleForTesting
    data class ListItem(
        val kindOfItem: KindOfItem,
        @DrawableRes val image: Int,
        @StringRes val title: Int,
        val description: String,
        val infoVisible: Boolean = false
    ) : java.io.Serializable {
        enum class KindOfItem {
            CreditorCompany,
            PaymentObject,
            RefreshedImport,
            AdviseCode,
            CreditorFiscalCode
        }
    }

    private fun QrCodeVerifyResponse.toListItems(): List<ListItem> {
        return listOf(
            ListItem(
                ListItem.KindOfItem.CreditorCompany,
                R.drawable.icon_creditor_company,
                R.string.label_payee,
                this.company
            ),
            ListItem(
                ListItem.KindOfItem.PaymentObject,
                R.drawable.icon_payment_object,
                R.string.label_paymentReason,
                this.description
            ),
            ListItem(
                ListItem.KindOfItem.RefreshedImport,
                R.drawable.icon_euro_grey,
                R.string.label_updatedAmount,
                this.amountFormatted(),
                true
            ),
            ListItem(
                ListItem.KindOfItem.AdviseCode,
                R.drawable.icon_advise_code,
                R.string.label_noticeCode,
                this.noticeNumber
            ),
            ListItem(
                ListItem.KindOfItem.CreditorFiscalCode,
                R.drawable.icon_company_fiscal_code,
                R.string.label_payeeTaxCode,
                this.paTaxCode
            )
        )
    }

    private inner class Adapter(list: List<ListItem>) : BaseRecyclerView<ListItem, ItemPaymentBinding>(list) {
        override fun viewBinding() = binding(ItemPaymentBinding::inflate)
        override fun bind(context: Context, item: ListItem, pos: Int, binding: ItemPaymentBinding) {
            binding.ivItem.setImageResource(item.image)
            binding.ivItemInfo.isVisible = item.infoVisible
            binding.ivItemInfo.setOnClickListener {
                UiKitStyledDialog.withStyle(Style.Info)
                    .withTitle(context.resources?.getString(R.string.title_differentAmount).orEmptyCharSeq())
                    .withDescription(context.resources?.getString(R.string.paragraph_differentAmount).orEmptyCharSeq())
                    .withMainBtn(context.resources?.getString(R.string.cta_okay).orEmptyCharSeq())
                    .showDialog(mainActivity?.supportFragmentManager)
            }
            binding.itemTitle.text = context.resources?.getString(item.title)?.uppercase().orEmptyCharSeq()
            binding.itemDescription.text = item.description
            if (item.kindOfItem == ListItem.KindOfItem.CreditorFiscalCode)
                binding.itemDivider.isVisible = false
        }
    }

    companion object {
        const val qrCodeParam = "qrCode"
        const val fromManuallyParam = "fromManually"
    }
}