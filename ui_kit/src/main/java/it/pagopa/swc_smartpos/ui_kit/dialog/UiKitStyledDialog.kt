@file:Suppress("UNUSED")

package it.pagopa.swc_smartpos.ui_kit.dialog

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.UiKitStyledDialogBinding
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.getColorSafely
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UiKitStyledDialog private constructor() : BaseUiKitDialog<UiKitStyledDialogBinding>() {
    override fun viewBinding() = binding(UiKitStyledDialogBinding::inflate)
    private val _isLoading = MutableStateFlow(false)
    private val isLoading = _isLoading.asStateFlow()
    private var style = Style.Info
    private var withCloseButton = false
    override var actionFoldable: (() -> Unit)? = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            this.updateView(this, binding.root, R.layout.ui_kit_styled_dialog)
    }

    override fun setupUI() {
        with(binding) {
            this@UiKitStyledDialog.title?.let {
                title.text = it
            } ?: run {
                title.isVisible = false
            }
            this@UiKitStyledDialog.description?.let {
                description.text = it
            } ?: run {
                description.isVisible = false
            }
            when (style) {
                Style.Success -> {
                    context.getDrawableSafely(R.drawable.styled_dialog_background_success)?.let { mainBackground.background = it }
                    dialogIv.setImageDrawable(context.getDrawableSafely(R.drawable.success_image))
                    context.getColorSafely(R.color.success_dark)?.let {
                        title.setTextColor(it);description.setTextColor(it);firstAction.setTextColor(it)
                    }
                    context.getDrawableSafely(R.drawable.rounded_success_dark_filled_8dp)?.let {
                        firstAction.background = it; firstActionCustomButton.background = it
                    }
                    context.getDrawableSafely(R.drawable.rounded_success_light_filled_8dp)?.let {
                        secondActionCustomButton.background = it
                    }
                    context.getColorSafely(R.color.success_dark)?.let {
                        secondAction.setTextColor(it);secondActionCustomButton.setTextColor(it)
                    }
                }

                Style.Info -> {
                    context.getDrawableSafely(R.drawable.styled_dialog_background_info)?.let { mainBackground.background = it }
                    dialogIv.setImageDrawable(context.getDrawableSafely(R.drawable.info_image))
                    context.getColorSafely(R.color.info_dark)?.let {
                        title.setTextColor(it);description.setTextColor(it);firstAction.setTextColor(it)
                    }
                    context.getDrawableSafely(R.drawable.rounded_info_dark_filled_8dp)?.let {
                        firstAction.background = it; firstActionCustomButton.background = it
                    }
                    context.getDrawableSafely(R.drawable.rounded_info_light_filled_8dp)?.let {
                        secondActionCustomButton.background = it
                    }
                    context.getColorSafely(R.color.info_dark)?.let {
                        secondAction.setTextColor(it);secondActionCustomButton.setTextColor(it)
                    }
                }

                Style.Warning -> {
                    context.getDrawableSafely(R.drawable.styled_dialog_background_warning)?.let { mainBackground.background = it }
                    dialogIv.setImageDrawable(context.getDrawableSafely(R.drawable.warning_image))
                    context.getColorSafely(R.color.warning_dark)?.let {
                        title.setTextColor(it);description.setTextColor(it);firstAction.setTextColor(it)
                    }
                    context.getDrawableSafely(R.drawable.rounded_warning_dark_filled_8dp)?.let {
                        firstAction.background = it; firstActionCustomButton.background = it
                    }
                    context.getDrawableSafely(R.drawable.rounded_warning_light_8dp)?.let {
                        secondActionCustomButton.background = it
                    }
                    context.getColorSafely(R.color.warning_dark)?.let {
                        secondAction.setTextColor(it);secondActionCustomButton.setTextColor(it)
                    }
                }

                Style.Error -> {
                    context.getDrawableSafely(R.drawable.styled_dialog_background_error)?.let { mainBackground.background = it }
                    dialogIv.setImageDrawable(context.getDrawableSafely(R.drawable.alert_image))
                    context.getColorSafely(R.color.error_dark)?.let {
                        title.setTextColor(it);description.setTextColor(it);firstAction.setTextColor(it)
                    }
                    context.getDrawableSafely(R.drawable.rounded_error_dark_filled_8dp)?.let {
                        firstAction.background = it; firstActionCustomButton.background = it
                    }
                    context.getDrawableSafely(R.drawable.rounded_error_light_filled_8dp)?.let {
                        secondActionCustomButton.background = it
                    }
                    context.getColorSafely(R.color.error_dark)?.let {
                        secondAction.setTextColor(it);secondActionCustomButton.setTextColor(it)
                    }
                }
            }
            animatedProgress.setKindOfProgress(style)
            mainBtnText?.let {
                firstAction.text = it
                context.getColorSafely(R.color.white)?.let { white -> firstAction.setTextColor(white) }
                context.getDrawableSafely(mainBtnDrawableEnd)?.let { endDrawable ->
                    firstAction.compoundDrawablePadding = nineDp()
                    firstAction.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null)
                }
                firstAction.setOnClickListener { if (dismissOnMainBtnClick) this@UiKitStyledDialog.dismiss(); mainBtnAction?.invoke() }
            }
            secondaryBtnText?.let {
                secondAction.text = it
                secondAction.setOnClickListener { if (dismissOnSecondaryBtnClick) this@UiKitStyledDialog.dismiss(); secondaryBtnAction?.invoke() }
            }
        }
        if (withCloseButton)
            setupCloseButton()
        setupCustomButtons()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewLifecycleOwner.lifecycleScope.launch {
            isLoading.collectLatest {
                binding.animatedProgress.isVisible = isLoading.value
                binding.firstAction.isVisible = !isLoading.value && mainBtnText != null
                binding.secondAction.isVisible = secondaryBtnText != null
                if (style == Style.Info)
                    if (it)
                        binding.dialogIv.setImageResource(R.drawable.icon_info_loading)
                    else
                        binding.dialogIv.setImageResource(R.drawable.info_image)
            }
        }
    }

    fun loading(load: Boolean) {
        _isLoading.value = load
    }

    fun withDismissAction(action: () -> Unit) = apply {
        this.dismissAction = action
    }

    @JvmName("withMainBtn1")
    fun withMainBtn(mainBtnText: CharSequence, @DrawableRes btnDrawableEnd: Int? = null, action: (() -> Unit)? = null) = apply {
        this.mainBtnText = mainBtnText
        this.mainBtnDrawableEnd = btnDrawableEnd
        this.mainBtnAction = action
    }

    fun withSecondaryBtn(secondaryBtnText: CharSequence, action: (() -> Unit)? = null) = apply {
        this.secondaryBtnText = secondaryBtnText
        this.secondaryBtnAction = action
    }

    @JvmName("withTitle1")
    fun withTitle(title: CharSequence) = apply {
        this.title = title
    }

    fun withDescription(description: CharSequence) = apply {
        this.description = description
    }

    @JvmName("withStyle1")
    fun withStyle(style: Style) = apply {
        this.style = style
    }

    override fun showDialog(manager: FragmentManager?, name: String?) {
        super.showDialog(manager, name ?: this.javaClass.name)
    }

    @JvmName("withMainCustomBtn1")
    fun withMainCustomBtn(customizer: CustomBtnCustomizer) = apply {
        this.mainCustomBtn = customizer
    }

    fun withSecondaryCustomBtn(customizer: CustomBtnCustomizer) = apply {
        this.secondaryCustomBtn = customizer
    }

    private fun setupCloseButton() {
        binding.closeStyledDialogIv.setImageResource(
            when (style) {
                Style.Info -> R.drawable.icon_close_info
                Style.Success -> R.drawable.icon_close_success
                Style.Error -> R.drawable.icon_close_error
                Style.Warning -> R.drawable.icon_close_warning
            }
        )
        binding.closeStyledDialog.isVisible = true
        binding.closeStyledDialog.setOnClickListener {
            this.dismiss()
        }
    }

    fun withClose() = apply {
        this.withCloseButton = true
    }

    companion object {
        @JvmStatic
        fun withStyle(style: Style) = UiKitStyledDialog().apply {
            this.style = style
        }

        @JvmStatic
        fun withTitle(title: CharSequence) = UiKitStyledDialog().apply {
            this.title = title
        }

        @JvmStatic
        fun withMainCustomBtn(customizer: CustomBtnCustomizer) = UiKitStyledDialog().apply {
            this.mainCustomBtn = customizer
        }

        @JvmStatic
        fun withMainBtn(mainBtnText: CharSequence, @DrawableRes btnDrawableEnd: Int? = null, action: (() -> Unit)? = null) = UiKitStyledDialog().apply {
            this.mainBtnText = mainBtnText
            this.mainBtnDrawableEnd = btnDrawableEnd
            this.mainBtnAction = action
        }
    }
}