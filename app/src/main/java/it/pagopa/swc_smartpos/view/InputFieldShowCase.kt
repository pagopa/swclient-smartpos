package it.pagopa.swc_smartpos.view

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.databinding.InputFieldShowCaseBinding
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp

class InputFieldShowCase : BaseDataBindingFragmentApp<InputFieldShowCaseBinding>() {
    override val backPress: () -> Unit get() = { findNavController().navigateUp() }
    override fun viewBinding() = binding(InputFieldShowCaseBinding::inflate)
    override fun setupUI() {
        binding.inputField11.actionDone = { text, _ ->
            text.toToast()
        }
        binding.inputField18.actionDone = { text, _ ->
            text.toToast()
            binding.inputField11NoCtrl.setFocus(true)
        }
        binding.inputField5.actionDone = { text, _ ->
            text.toToast()
        }
        binding.inputField11NoCtrl.actionDone = { _, _ ->
            binding.inputField11NoCtrl.getText()?.toToast()
        }
    }

    private fun String.toToast() {
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }
}