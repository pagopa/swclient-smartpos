package it.pagopa.swc_smartpos.view

import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.databinding.ButtonsShowCaseBinding
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp

class ButtonsShowCase : BaseDataBindingFragmentApp<ButtonsShowCaseBinding>() {
    override val backPress: () -> Unit get() = { findNavController().navigateUp() }
    override fun viewBinding() = binding(ButtonsShowCaseBinding::inflate)
    override fun setupListeners() {
        with(binding) {
            infoDarkBtn.setOnClickListener {
                infoDarkBtn.showLoading(!infoDarkBtn.isLoading)
            }
            infoLightBtn.setOnClickListener {
                infoLightBtn.showLazyLoading(!infoLightBtn.isLoading)
            }
            successDarkBtn.setOnClickListener {
                successDarkBtn.showLoading(!successDarkBtn.isLoading)
            }
            successLightBtn.setOnClickListener {
                successLightBtn.showLoading(!successLightBtn.isLoading)
            }
            errorDarkBtn.setOnClickListener {
                errorDarkBtn.showLoading(!errorDarkBtn.isLoading)
            }
            errorLightBtn.setOnClickListener {
                errorLightBtn.showLoading(!errorLightBtn.isLoading)
            }
        }
    }

    //nothing to do
    override fun setupUI() {
    }
}