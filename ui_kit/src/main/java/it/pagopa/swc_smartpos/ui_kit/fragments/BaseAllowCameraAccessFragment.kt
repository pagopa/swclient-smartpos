package it.pagopa.swc_smartpos.ui_kit.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.AllowCameraAccessBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment

abstract class BaseAllowCameraAccessFragment<Act : AppCompatActivity> : BaseDataBindingFragment<AllowCameraAccessBinding, Act>() {
    abstract val closeAction: () -> Unit
    abstract fun mainBtnAction(): () -> Unit
    override fun viewBinding() = binding(AllowCameraAccessBinding::inflate)
    override fun setupListeners() {
        binding.closeBtnAllowCameraAccess.setOnClickListener {
            closeAction.invoke()
        }
        binding.whiteButtonAllowCamera.setOnClickListener {
            mainBtnAction().invoke()
        }
    }

    fun buildUi(state: State) {
        if (state == State.First)
            firstUi()
        else
            secondUi()
    }

    private fun firstUi() {
        binding.titleAllowAccessCamera.text = getTextSafely(R.string.title_allowCamera)
        binding.descriptionAllowAccessCamera.text = getTextSafely(R.string.paragraph_allowCamera)
        binding.whiteButtonAllowCamera.text = getTextSafely(R.string.cta_allow)
        binding.llAllowCameraFakeToast.isVisible = true
    }

    private fun secondUi() {
        binding.titleAllowAccessCamera.text = getTextSafely(R.string.title_cameraDenied)
        binding.descriptionAllowAccessCamera.text = getTextSafely(R.string.paragraph_contactPosProvider)
        binding.whiteButtonAllowCamera.text = getTextSafely(R.string.cta_enterManually)
        binding.llAllowCameraFakeToast.isVisible = false
    }

    enum class State {
        First, Second
    }
}