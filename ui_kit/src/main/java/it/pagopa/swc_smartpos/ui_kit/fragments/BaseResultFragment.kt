package it.pagopa.swc_smartpos.ui_kit.fragments

import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.ResultBinding
import it.pagopa.swc_smartpos.ui_kit.buttons.CustomDrawableButton
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getColorSafely
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely

abstract class BaseResultFragment<Act : AppCompatActivity> : BaseDataBindingFragment<ResultBinding, Act>() {
    abstract var state: State
    open var firstButton: CustomBtnCustomizer? = null
    open var secondBtn: CustomBtnCustomizer? = null
    var title: Int = 0
    var description: Int = 0
    var descriptionArgument: String = ""
    var titleArgument: String = ""
    override fun viewBinding() = binding(ResultBinding::inflate)
    private fun getIvHere(id: Int): Drawable? = mainActivity?.let { ContextCompat.getDrawable(it, id) }

    private fun CustomDrawableButton.forFirst(state: State, btnCustomizer: CustomBtnCustomizer? = null) {
        when (state) {
            State.Success -> context.getDrawableSafely(R.drawable.rounded_success_dark_filled_8dp)?.let {
                this.background = it
            }

            State.Error -> context.getDrawableSafely(R.drawable.rounded_error_dark_filled_8dp)?.let {
                this.background = it
            }

            State.Info -> context.getDrawableSafely(R.drawable.rounded_info_dark_filled_8dp)?.let {
                this.background = it
            }

            State.Warning -> context.getDrawableSafely(R.drawable.rounded_warning_dark_filled_8dp)?.let {
                this.background = it
            }
        }
        btnCustomizer?.let {
            this.setText(it.text)
            if (it.drawable != null)
                this.setIv(getIvHere(it.drawable), CustomDrawableButton.IconGravity.fromBoolean(it.isStart))
            else
                this.setIv(null)
        } ?: run {
            this.isVisible = false
        }
    }

    private fun CustomDrawableButton.forSecond(state: State, btnCustomizer: CustomBtnCustomizer? = null) {
        when (state) {
            State.Success -> {
                context.getColorSafely(R.color.success_dark)?.let {
                    this.setTextColor(it)
                }
                context.getDrawableSafely(R.drawable.rounded_success_light_stroke_8dp)?.let {
                    this.background = it
                }
            }

            State.Error -> {
                context.getColorSafely(R.color.error_dark)?.let {
                    this.setTextColor(it)
                }
                context.getDrawableSafely(R.drawable.rounded_error_light_stroke_8dp)?.let {
                    this.background = it
                }
            }

            State.Info -> {
                context.getColorSafely(R.color.info_dark)?.let {
                    this.setTextColor(it)
                }
                context.getDrawableSafely(R.drawable.rounded_info_light_stroke_8dp)?.let {
                    this.background = it
                }
            }

            State.Warning -> {
                context.getColorSafely(R.color.warning_dark)?.let {
                    this.setTextColor(it)
                }
                context.getDrawableSafely(R.drawable.rounded_warning_light_stroke_8dp)?.let {
                    this.background = it
                }
            }
        }
        btnCustomizer?.let {
            this.setText(it.text)
            if (it.drawable != null)
                this.setIv(getIvHere(it.drawable), CustomDrawableButton.IconGravity.fromBoolean(it.isStart))
            else
                this.setIv(null)
        } ?: run {
            this.isVisible = false
            val layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            val marginTop = context.resources.getDimension(R.dimen.cta_margin_top)
            layoutParams.setMargins(0, marginTop.toInt(), 0, 0)
            binding.btnContinue.layoutParams = layoutParams
        }
    }

    override fun setupUI() {
        when (state) {
            State.Success -> {
                binding.ivOperation.setImageResource(R.drawable.success_image)
                context.getColorSafely(R.color.success_dark)?.let { binding.tvTitle.setTextColor(it) }
                binding.tvDescription.isVisible = description != 0
                context.getColorSafely(R.color.success_light)?.let { binding.root.setBackgroundColor(it) }
            }

            State.Error -> {
                binding.ivOperation.setImageResource(R.drawable.alert_image)
                context.getColorSafely(R.color.error_dark)?.let { binding.tvTitle.setTextColor(it); binding.tvDescription.setTextColor(it) }
                binding.tvDescription.isVisible = true
                context.getColorSafely(R.color.error_light)?.let { binding.root.setBackgroundColor(it) }
            }

            State.Info -> {
                binding.ivOperation.setImageResource(R.drawable.info_image)
                context.getColorSafely(R.color.info_dark)?.let { binding.tvTitle.setTextColor(it); binding.tvDescription.setTextColor(it) }
                binding.tvDescription.isVisible = true
                context.getColorSafely(R.color.info_light)?.let { binding.root.setBackgroundColor(it) }
            }

            State.Warning -> {
                binding.ivOperation.setImageResource(R.drawable.warning_image)
                context.getColorSafely(R.color.warning_dark)?.let { binding.tvTitle.setTextColor(it); binding.tvDescription.setTextColor(it) }
                binding.tvDescription.isVisible = true
                context.getColorSafely(R.color.warning_light)?.let { binding.root.setBackgroundColor(it) }
            }
        }
        binding.btnContinue.forFirst(state, firstButton)
        binding.secondBtn.forSecond(state, secondBtn)
        if (title != 0) {
            val rawText = getTextSafely(title)
            val titleHere = if (rawText.contains("%s"))
                getStringSafelyWithOneArg(title, titleArgument)
            else
                rawText
            binding.tvTitle.text = titleHere
        }
        if (description != 0) {
            binding.tvDescription.isVisible = true
            val rawText = getTextSafely(description)
            val descriptionHere = if (rawText.contains("%s"))
                getStringSafelyWithOneArg(description, descriptionArgument)
            else
                rawText
            binding.tvDescription.text = descriptionHere
        } else
            binding.tvDescription.isVisible = false
    }

    override fun setupListeners() {
        firstButton?.action?.let { action ->
            binding.btnContinue.setOnClickListener {
                action.invoke(this)
            }
        }
        secondBtn?.action?.let { action ->
            binding.secondBtn.setOnClickListener {
                action.invoke(this)
            }
        }
    }

    enum class State : java.io.Serializable {
        Success,
        Error,
        Info,
        Warning
    }

    companion object {
        const val stateArg = "State"
        const val titleArg = "title"
        const val titleArgumentConstant = "descriptionArgument"
        const val descriptionArg = "description"
        const val descriptionArgumentConstant = "descriptionArgument"
        const val firstButtonArg = "firstButtonArg"
        const val secondButtonArg = "secondButtonArg"
    }
}