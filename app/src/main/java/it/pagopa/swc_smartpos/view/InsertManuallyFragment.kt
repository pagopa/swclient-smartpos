package it.pagopa.swc_smartpos.view

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.InsertManuallyBinding
import it.pagopa.swc_smartpos.network.NetworkWrapper
import it.pagopa.swc_smartpos.second_screen.showSecondScreenInsertManually
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smartpos.ui_kit.utils.disablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.enablePrimaryButton
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view_model.InsertManuallyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InsertManuallyFragment : BaseDataBindingFragmentApp<InsertManuallyBinding>() {
    private val viewModel: InsertManuallyViewModel by viewModels()
    override fun viewBinding() = binding(InsertManuallyBinding::inflate)
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
            null,
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
                this.backToIntroFragment()
            },
            R.color.white
        )
    override val backPress: () -> Unit
        get() = {
            viewModel.backPressAction.value.invoke()
        }

    override fun setupUI() {
        binding.inputTextInsertManually.automaticAction = true
        viewModel.setButtonVisibility(false)
        viewModel.setButtonModel(ButtonModel(false) {})
        viewModel.setBackPressAction { this.backToIntroFragment() }
        mainActivity?.showSecondScreenInsertManually()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest {
                if (it == State.AdviseCode) {
                    viewModel.setBackPressAction { this@InsertManuallyFragment.backToIntroFragment() }
                    binding.titleInsertManually.text = getStringSafely(R.string.title_enterNoticeCode)
                    binding.descriptionInsertManually.text = getStringSafely(R.string.paragraph_noticeCode)
                    binding.inputTextInsertManually.forAdviseCode()
                } else {
                    binding.inputTextInsertManually.fiscalCodeHintAndText()
                    this.launch {
                        delay(500L)
                        binding.inputTextInsertManually.forFiscalCode()
                    }
                    viewModel.setBackPressAction {
                        binding.inputTextInsertManually.automaticAction = false
                        viewModel.setButtonVisibility(true)
                        viewModel.setState(State.AdviseCode)
                    }
                    binding.titleInsertManually.text = getStringSafely(R.string.title_enterPayeeTaxCode)
                    binding.descriptionInsertManually.text = getStringSafely(R.string.paragraph_payeeTaxCode)
                }
            }
        }
        viewModel.buttonModel.observe(viewLifecycleOwner) { btn ->
            binding.btnConfirm.isEnabled = btn.enabled
            if (btn.enabled)
                binding.btnConfirm.enablePrimaryButton(context)
            else
                binding.btnConfirm.disablePrimaryButton(context)
            binding.btnConfirm.setOnClickListener {
                btn.action.invoke()
            }
        }
        viewModel.buttonVisible.observe(viewLifecycleOwner) {
            binding.btnConfirm.isVisible = it
        }
    }

    private fun InputText.forAdviseCode() {
        mainActivity?.resources?.getText(R.string.label_noticeCode)?.let { adviseCode ->
            this.setHint(adviseCode)
        }
        this.setMaxLength(18)
        this.delayActionDone = 500L
        this.actionDone = { adviseCode, itsOk ->
            viewModel.setAdviseCode(adviseCode)
            if (itsOk) {
                binding.inputTextInsertManually.automaticAction = true
                viewModel.setButtonVisibility(false)
                viewModel.setState(State.FiscalCode)
            }
        }
        this.onAction = { adviseCode, itsOk ->
            if (itsOk) {
                viewModel.setButtonModel(ButtonModel(true) {
                    binding.inputTextInsertManually.automaticAction = true
                    viewModel.setButtonVisibility(false)
                    viewModel.setAdviseCode(adviseCode)
                    viewModel.setState(State.FiscalCode)
                })
            } else {
                viewModel.setButtonModel(ButtonModel(false) {})
            }
        }
        this.setText(viewModel.adviseCode.value)
        this.checkState()
        this.setFocus(true)
    }

    private fun fiscalCodeActionDone(creditorFiscalCode: String, itsOk: Boolean) {
        viewModel.setCreditorFiscalCode(creditorFiscalCode)
        if (itsOk) {
            binding.inputTextInsertManually.automaticAction = false
            verifyPayment(creditorFiscalCode)
        }
    }

    private fun verifyPayment(creditorFiscalCode: String) {
        mainActivity?.let { act ->
            act.viewModel.setLoaderText(getStringSafely(R.string.feedback_loading_verifyNotice))
            act.viewModel.showLoader(true to true)
            accessTokenLambda { accessToken ->
                viewModel.verifyPayment(
                    act, creditorFiscalCode, viewModel.adviseCode.value, accessToken,
                    act.sdkUtils?.getCurrentBusiness()?.value
                ).observe(viewLifecycleOwner, NetworkWrapper(act, {
                    findNavController().navigate(R.id.action_insertManuallyFragment_to_paymentResumeFragment, Bundle().apply {
                        putSerializable(PaymentResumeFragment.qrCodeParam, it)
                        putBoolean(PaymentResumeFragment.fromManuallyParam, true)
                    })
                }, {
                    if (it == BaseWrapper.tokenRefreshed)
                        verifyPayment(creditorFiscalCode)
                    else
                        viewModel.setButtonVisibility(true)
                }, isForQrCode = true, doErrorActionOnNoNetwork = true))
            }
        }
    }

    private fun InputText.fiscalCodeHintAndText(){
        mainActivity?.resources?.getText(R.string.label_payeeTaxCode)?.let { creditorFiscalCode ->
            this.setHint(creditorFiscalCode)
        }
        this.setText(viewModel.creditorFiscalCode.value)
    }

    private fun InputText.forFiscalCode() {
        this.setMaxLength(11)
        this.delayActionDone = 500L
        this.actionDone = { creditorFiscalCode, itsOk ->
            fiscalCodeActionDone(creditorFiscalCode, itsOk)
        }
        this.onAction = { creditorFiscalCode, itsOk ->
            if (itsOk) {
                viewModel.setButtonModel(ButtonModel(true) {
                    fiscalCodeActionDone(creditorFiscalCode, true)
                })
            } else {
                viewModel.setButtonModel(ButtonModel(false) {})
            }
        }
        this.checkState()
        this.setFocus(true)
    }

    data class ButtonModel(val enabled: Boolean, val action: () -> Unit) : java.io.Serializable
    enum class State {
        AdviseCode,
        FiscalCode
    }
}