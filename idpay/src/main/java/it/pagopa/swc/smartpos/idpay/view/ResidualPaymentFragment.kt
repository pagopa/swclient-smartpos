package it.pagopa.swc.smartpos.idpay.view

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.ResidualPaymentFragmentBinding
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view_model.ResidualPaymentViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.sharedutils.model.PaymentStatus
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class ResidualPaymentFragment : BaseDataBindingFragmentApp<ResidualPaymentFragmentBinding>() {
    private val viewModel: ResidualPaymentViewModel by viewModels()
    override val backPress: () -> Unit = {}
    override val layoutId: Int get() = R.layout.residual_payment_fragment
    override fun viewBinding() = binding(ResidualPaymentFragmentBinding::inflate)
    override val header: HeaderView = HeaderView(
        null,
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
            dialogBackHome.showDialog(mainActivity?.supportFragmentManager)
        },
        it.pagopa.swc.smartpos.app_shared.R.color.white
    )

    private val dialogBackHome by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.back_home_dialog_title))
            .withDescription(
                getStringSafelyWithOneArg(
                    R.string.back_home_dialog_description,
                    viewModel.model.value?.residual().orEmpty()
                )
            )
            .withMainBtn(getTextSafely(R.string.avoid))
            .withSecondaryBtn(getTextSafely(R.string.back_home_dialog_cta)) {
                this.backToIntroFragment()
            }
    }

    private val dialogPosApp by lazy {
        UiKitStyledDialog
            .withStyle(Style.Info)
            .withTitle(getStringSafely(it.pagopa.swc.smartpos.app_shared.R.string.title_continueOnPos))
            .withDescription(getStringSafely(it.pagopa.swc.smartpos.app_shared.R.string.paragraph_continueOnPos))
    }

    private val dialogHowToPay by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.how_to_pay_residual))
            .withCloseVisible()
            .withMainCustomBtn(CustomBtnCustomizer(
                getTextSafely(R.string.cta_pay_card),
                R.drawable.credit_card, true, this
            ) {
                dialogPosApp.loading(true)
                dialogPosApp.showDialog(mainActivity?.supportFragmentManager)
                val saleModel = mainActivity?.viewModel?.model?.value
                saleModel?.amount?.minus(saleModel.availableSale ?: 0L)?.let { residual ->
                    mainActivity?.sdkUtils?.launchPayment(residual, "payment_app_id_pay")
                }
            }).withSecondaryCustomBtn(CustomBtnCustomizer(
                getTextSafely(R.string.cta_pay_cash),
                R.drawable.wallet, true, this
            ) {
                (it as ResidualPaymentFragment).viewModel.setPayWithCashChosen(true)
            })
    }

    private val dialogCancelOp by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.dialog_cancel_op_title))
            .withDescription(getTextSafely(R.string.dialog_cancel_op_description))
            .withMainBtn(getTextSafely(R.string.dialog_cancel_op_delete))
            .withSecondaryBtn(getTextSafely(R.string.cancel_op)) {
                deleteTransaction()
            }
    }

    private fun deleteTransaction() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.cancel_op_loader))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.cancelOp(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                mainActivity?.viewModel?.model?.value?.milTransactionId.orEmpty()
            ).observe(viewLifecycleOwner, BaseWrapper(mainActivity!!,
                successAction = {
                    mainActivity?.viewModel?.setModel(
                        SaleModel(
                            timeStamp = it?.lastUpdate,
                            isCanceledOp = true
                        )
                    )
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
        viewModel.setModel(
            arguments?.getSerializableExtra(
                fragModel,
                ResidualPaymentModel::class.java
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun setupUI() {
        super.setupUI()
        viewModel.viewModelScope.launch {
            viewModel.model.collectLatest {
                it?.let { model ->
                    binding.costText.text = model.cost()
                    binding.bonusText.text = "- ${model.bonus()}"
                    binding.residualValue.text = model.residual()
                    binding.btnPay.text =
                        getStringSafelyWithOneArg(R.string.pay_s, model.residual())
                }
            }
        }
    }

    override fun setupListeners() {
        binding.btnCancelOp.setOnClickListener {
            dialogCancelOp.showDialog(mainActivity?.supportFragmentManager)
        }
        binding.btnPay.setOnClickListener {
            if (BuildConfig.FLAVOR.contains("androidNative", true))
                this.viewModel.setPayWithCashChosen(true)
            else
                dialogHowToPay.showDialog(mainActivity?.supportFragmentManager)
        }
    }

    override fun setupObservers() {
        viewModel.viewModelScope.launch {
            viewModel.payWithCashChosen.collectLatest {
                if (it) {
                    findNavController().navigate(
                        R.id.action_residualPaymentFragment_to_outroFragment,
                        bundleOf(OutroFragment.isPayWithCashChosen to true)
                    )
                }
            }
        }
        viewModel.viewModelScope.launch {
            viewModel.paidWithCard.collectLatest {
                if (it) {
                    mainActivity?.viewModel?.setModel(SaleModel(percentageSale = 100f))
                    findNavController().navigate(R.id.action_residualPaymentFragment_to_outroFragment)
                }
            }
        }
        mainActivity?.viewModel?.payment?.observe(viewLifecycleOwner) {
            it?.let { payment ->
                if (dialogPosApp.isVisible)
                    dialogPosApp.dismiss()
                WrapperLogger.i("Payment", payment.status.name)
                when (payment.status) {
                    PaymentStatus.COMPLETED -> viewModel.setPaidWithCard(true)
                    else -> {
                    }
                }
                mainActivity?.viewModel?.setPayment(null)
            }
        }
    }

    data class ResidualPaymentModel(private val totalCost: Long, private val idPayBonus: Long) :
        Serializable {
        fun cost() = totalCost.toAmountFormatted()
        fun bonus() = idPayBonus.toAmountFormatted()
        fun residual() = (totalCost - idPayBonus).toAmountFormatted()
    }

    companion object {
        const val fragModel = "fragModel"
    }
}