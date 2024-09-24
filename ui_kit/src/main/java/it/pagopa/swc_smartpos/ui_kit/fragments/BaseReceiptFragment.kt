package it.pagopa.swc_smartpos.ui_kit.fragments

import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import it.pagopa.swc_smart_pos.ui_kit.databinding.ReceiptBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely

abstract class BaseReceiptFragment<Act : AppCompatActivity> : BaseDataBindingFragment<ReceiptBinding, Act>() {
    abstract val mainText: Int
    abstract val mainImage: Int
    open val secondaryText: Int? = null
    abstract val firstButton: CustomButton
    abstract val secondButton: CustomButton
    abstract val thirdButton: CustomButton
    override fun viewBinding() = binding(ReceiptBinding::inflate)
    override fun setupListeners() {
        binding.firstBtnReceipt.setOnClickListener {
            firstButton.action.invoke()
        }
        binding.secondBtnReceipt.setOnClickListener {
            secondButton.action.invoke()
        }
        binding.thirdBtnReceipt.setOnClickListener {
            thirdButton.action.invoke()
        }
    }
    @CallSuper
    override fun setupUI() {
        binding.mainImage.setImageResource(mainImage)
        binding.mainText.text = getTextSafely(mainText)
        secondaryText?.let {
            binding.secondaryText.text = getTextSafely(it)
        } ?: run {
            binding.secondaryText.isVisible = false
        }
        binding.firstBtnReceipt.setText(getTextSafely(firstButton.text))
        context.getDrawableSafely(firstButton.drawableLeft)?.let { startDrawable ->
            binding.firstBtnReceipt.setIv(startDrawable)
        }
        binding.secondBtnReceipt.setText(getTextSafely(secondButton.text))
        context.getDrawableSafely(secondButton.drawableLeft)?.let { startDrawable ->
            binding.secondBtnReceipt.setIv(startDrawable)
        }
        binding.thirdBtnReceipt.setText(getTextSafely(thirdButton.text))
        context.getDrawableSafely(thirdButton.drawableLeft)?.let { startDrawable ->
            binding.thirdBtnReceipt.setIv(startDrawable)
        }
    }

    data class CustomButton(@StringRes val text: Int, @DrawableRes val drawableLeft: Int, val action: () -> Unit) : java.io.Serializable
}