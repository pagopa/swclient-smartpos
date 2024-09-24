package it.pagopa.swc.smartpos.idpay.view

import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.OutroFragmentBinding
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenOutro
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.view_shared.acceptNewBonus
import it.pagopa.swc.smartpos.idpay.view_model.OutroViewModel
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.pagopa.swc.smartpos.app_shared.R as RShared

class OutroFragment : BaseDataBindingFragmentApp<OutroFragmentBinding>() {
    private val viewModel: OutroViewModel by viewModels()
    override val layoutId: Int get() = R.layout.outro_fragment
    override val backPress: () -> Unit get() = {}
    override fun viewBinding() = binding(OutroFragmentBinding::inflate)
    private fun dialogBackHome(residual: String): UiKitDialog {
        return UiKitDialog.withTitle(getTextSafely(R.string.back_home_dialog_title))
            .withDescription(
                getStringSafelyWithOneArg(
                    R.string.back_home_dialog_description,
                    residual
                )
            )
            .withMainBtn(getTextSafely(R.string.avoid))
            .withSecondaryBtn(getTextSafely(R.string.back_home_dialog_cta)) {
                backHome()
            }
    }

    private fun infoResidualDialog(residual: String): UiKitStyledDialog {
        return UiKitStyledDialog.withStyle(Style.Info)
            .withTitle(getTextSafely(R.string.residual_import_dialog_title))
            .withDescription(
                getStringSafelyWithOneArg(
                    R.string.residual_import_dialog_description,
                    residual
                )
            )
            .withClose()
            .withMainBtn(getTextSafely(R.string.residual_import_dialog_cta)) {
                payResidual()
            }
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        viewModel.setPayWithCashChosen(arguments?.getBoolean(isPayWithCashChosen) == true)
        viewModel.isFromDetail = arguments?.getBoolean(fromDetail) ?: false
    }

    override fun setupObservers() {
        viewModel.viewModelScope.launch {
            viewModel.payWithCashChosen.collectLatest {
                if (it)
                    binding.payWithCashChosenUi()
            }
        }
    }

    private fun oneMinuteMax() {
        viewModel.oneMinuteMaxInFragment {
            mainActivity?.viewModel?.voidModel()
            this.backToIntroFragment()
        }
    }

    override fun setupUI() {
        super.setupUI()
        if (viewModel.isFromDetail) {
            binding.description.text = getTextSafely(RShared.string.paragraph_receipt_printed)
            binding.btnAcceptNewBonus.text = getTextSafely(RShared.string.cta_goToHomepage)
            oneMinuteMax()
        } else {
            if (!viewModel.payWithCashChosen.value) {
                val isAllCovered =
                    mainActivity?.viewModel?.model?.value?.percentageSale == 100f || mainActivity?.viewModel?.model?.value?.isCanceledOp == true
                mainActivity?.showSecondScreenOutro(!isAllCovered)
                binding inflate isAllCovered
                if (isAllCovered)
                    oneMinuteMax()
            } else
                mainActivity?.showSecondScreenOutro(false)
        }
    }

    private fun payResidual() {
        findNavController().navigate(
            R.id.action_outroFragment_to_residualPaymentFragment,
            bundleOf(
                ResidualPaymentFragment.fragModel to ResidualPaymentFragment.ResidualPaymentModel(
                    mainActivity?.viewModel?.model?.value?.amount ?: 0L,
                    mainActivity?.viewModel?.model?.value?.availableSale ?: 0L
                )
            )
        )
    }

    private fun backHome() {
        mainActivity?.viewModel?.voidModel()
        this.backToIntroFragment()
    }

    override fun setupListeners() {
        binding.backHome.setOnClickListener {
            val saleModel = mainActivity?.viewModel?.model?.value
            val isAllCovered = saleModel?.percentageSale == 100f
            if (saleModel?.isCanceledOp == true || isAllCovered || viewModel.payWithCashChosen.value || viewModel.isFromDetail)
                backHome()
            else {
                val residualText = saleModel?.amount?.minus(saleModel.availableSale ?: 0L)
                dialogBackHome(
                    residualText?.toAmountFormatted().orEmpty()
                ).showDialog(mainActivity?.supportFragmentManager)
            }
        }
        binding.btnPayResidual.setOnClickListener {
            payResidual()
        }
        binding.btnAcceptNewBonus.setOnClickListener {
            if (viewModel.isFromDetail)
                backHome()
            else
                this.acceptNewBonus(this.viewModel)
        }
    }

    private infix fun OutroFragmentBinding.inflate(allCovered: Boolean) {
        this.btnPayResidual.isVisible = !allCovered
        this.btnAcceptNewBonus.isVisible = allCovered
        if (!allCovered) {
            val saleModel = mainActivity?.viewModel?.model?.value
            val residualText = saleModel?.amount?.minus(saleModel.availableSale ?: 0L)
            infoResidualDialog(
                residualText?.toAmountFormatted().orEmpty()
            ).showDialog(mainActivity?.supportFragmentManager)
        }
    }

    private fun OutroFragmentBinding.payWithCashChosenUi() {
        this.btnAcceptNewBonus.isVisible = true
        this.description.isVisible = false
        this.btnPayResidual.isVisible = false
        this.llResidualAfter.isVisible = true
        val saleModel = mainActivity?.viewModel?.model?.value
        val residualText = saleModel?.amount?.minus(saleModel.availableSale ?: 0L)
        this.tvResidual.text = residualText?.toAmountFormatted().orEmpty()
        this.mainTitle.text = getTextSafely(R.string.pay_with_cash_chosen_title)
        this.btnAcceptNewBonus.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0, context?.dpToPx(48f) ?: 0, 0, 0)
        }
    }

    companion object {
        const val isPayWithCashChosen = "isPayWithCashChosen"
        const val fromDetail = "fromDetailArg"
    }
}