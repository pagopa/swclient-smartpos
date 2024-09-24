package it.pagopa.swc_smartpos.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalRequest
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalResponse
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseFormFragment
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.disablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.enablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.hideKeyboard
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view_model.HelpedWaySubscribeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class HelpedWaySubscribeFragment : BaseFormFragment<MainActivity>() {
    private val viewModel: HelpedWaySubscribeViewModel by viewModels()
    override val backPress: () -> Unit = { findNavController().popBackStack(R.id.introFragment, false) }
    private val input by lazy {
        InputText(requireContext()).apply {
            setInputType(InputText.InputTypeInputText.Text)
            inputTypeOnlyAlphaNumeric()
            setHint(getTextSafely(R.string.helped_way_hint))
            setTextChunk(0)
            onAction = { text, _ ->
                viewModel.setInput(text)
            }
            actionDone = { _, _ ->
                if (binding.btnForm.isEnabled)
                    binding.btnForm.performClick()
            }
        }
    }
    override val inputTextArrays: Array<InputText> by lazy { arrayOf(input) }
    override val image: Int = RUiKit.drawable.logo_blu
    override val title: Int = R.string.helped_way_title
    override val buttonText: Int = R.string.cta_continue
    override val buttonAction: () -> Unit = {
        if (!binding.btnForm.isLoading) {
            mainActivity.subscribeTerminal()
            context.hideKeyboard()
        }
    }

    override fun setupUI() {
        super.setupUI()
        input.setFocus(true)
    }

    private fun SubscribeTerminalResponse?.applyLogic(): Boolean {
        if (this == null || this.location.isEmpty()) return false
        val split = this.location[0].split("/")
        val size = split.size - 1
        WrapperLogger.d("TerminalID", split[size])
        mainActivity?.setSubscriberId(split[size])//Saving to sharedPreference...
        return true
    }

    private fun okLogic() {
        binding.btnForm.showLoading(false)
        mainActivity?.viewModel?.setHelpedWayDeactivated(false)
        this@HelpedWaySubscribeFragment.findNavController().navigate(R.id.action_global_resultFragment, Bundle().apply {
            putBoolean(ResultFragment.backHome, true)
            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Success)
            this.putInt(BaseResultFragment.titleArg, R.string.helped_way_ok_subscribe)
            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer(getTextSafely(R.string.cta_goToHomepage)) {
                this@HelpedWaySubscribeFragment.findNavController().popBackStack(R.id.introFragment, false)
            })
        })
    }

    private fun errorLogic() {
        binding.btnForm.showLoading(false)
        this@HelpedWaySubscribeFragment.findNavController().navigate(R.id.action_global_resultFragment, Bundle().apply {
            putBoolean(ResultFragment.backHome, true)
            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Error)
            this.putInt(BaseResultFragment.titleArg, R.string.helped_way_fail_subscribe)
            this.putInt(BaseResultFragment.descriptionArg, R.string.paragraph_tryAgain)
            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer(getTextSafely(R.string.cta_goToHomepage)) {
                this@HelpedWaySubscribeFragment.findNavController().popBackStack(R.id.introFragment, false)
            })
        })// Error
    }

    private fun MainActivity?.subscribeTerminal() {
        if (this == null) return
        val business = this.sdkUtils?.getCurrentBusiness()?.value
        this@HelpedWaySubscribeFragment.binding.btnForm.showLoading(true)
        accessTokenLambda { bearer ->
            this@HelpedWaySubscribeFragment.viewModel.subscribeTerminal(
                mainActivity!!,
                bearer,
                SubscribeTerminalRequest(
                    this@HelpedWaySubscribeFragment.viewModel.input.value,
                    business?.paTaxCode.orEmpty()
                ),
                business
            ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
                if (it.applyLogic())
                    okLogic()
                else
                    errorLogic()
            }, errorAction = {
                if (it == BaseWrapper.tokenRefreshed)
                    this@HelpedWaySubscribeFragment.mainActivity.subscribeTerminal()
                else
                    errorLogic()
            }, showLoader = false, showDialog = false, showSecondScreenLoader = false))
        }
    }

    override fun setupObservers() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.input.collectLatest {
                if (it.isNotEmpty())
                    binding.btnForm.enablePrimaryButton(context)
                else
                    binding.btnForm.disablePrimaryButton(context)
            }
        }
    }

    companion object {
        const val subscriberId = "subscriberId"
    }
}