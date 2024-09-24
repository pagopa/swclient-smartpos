package it.pagopa.swc_smartpos.ui_kit.fragments

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import it.pagopa.swc_smart_pos.ui_kit.databinding.OutroBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment

abstract class BaseOutroFragment<Act : AppCompatActivity> : BaseDataBindingFragment<OutroBinding, Act>() {
    abstract val homeAction: () -> Unit
    abstract val mainText: Int
    abstract var descriptionText: Int
    abstract var btnText: Int
    abstract val btnAction: () -> Unit
    override fun viewBinding() = binding(OutroBinding::inflate)

    @CallSuper
    override fun setupUI() {
        binding.mainText.text = getTextSafely(mainText)
        binding.tvDescription.text = getTextSafely(descriptionText)
        binding.btnNewPayment.text = getTextSafely(btnText)
    }

    override fun setupListeners() {
        binding.backHome.setOnClickListener { homeAction.invoke() }
        binding.btnNewPayment.setOnClickListener { btnAction.invoke() }
    }
}