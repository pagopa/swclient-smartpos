package it.pagopa.swc_smartpos.ui_kit.fragments


import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.BaseFormLayoutBinding
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely
import kotlin.math.roundToInt

abstract class BaseFormFragment<Act : AppCompatActivity> : BaseDataBindingFragment<BaseFormLayoutBinding, Act>() {
    abstract val inputTextArrays: Array<InputText>

    @get:DrawableRes
    abstract val image: Int

    @get:StringRes
    abstract val title: Int

    @get:StringRes
    abstract val buttonText: Int
    abstract val buttonAction: () -> Unit
    override fun viewBinding() = binding(BaseFormLayoutBinding::inflate)

    @CallSuper
    override fun setupListeners() {
        binding.btnForm.setOnClickListener {
            buttonAction.invoke()
        }
    }

    fun updateLLInputs() {
        val inputTextParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        inputTextArrays.forEachIndexed { index, it ->
            val currentText = it.getText().orEmpty()
            if (it.parent != null)
                (it.parent as? ViewGroup)?.removeView(it)
            binding.llInputs.addView(it.apply {
                layoutParams = inputTextParam.apply {
                    if (index != 0)
                        setMargins(0, context.resources.getDimension(R.dimen.header_margin_top).roundToInt(), 0, 0)
                }
                setText(currentText)
                context?.resources?.getDimension(R.dimen.body)?.let { dimen ->
                    editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen)
                } ?: run {
                    editText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            })
        }
    }

    @CallSuper
    override fun setupUI() {
        context.getDrawableSafely(image)?.let { binding.formImage.setImageDrawable(it) }
        binding.formTitle.text = getTextSafely(title)
        binding.btnForm.setButtonText(getTextSafely(buttonText))
        updateLLInputs()
    }
}