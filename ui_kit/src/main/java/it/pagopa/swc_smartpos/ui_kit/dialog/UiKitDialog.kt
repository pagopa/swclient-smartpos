@file:Suppress("UNUSED")

package it.pagopa.swc_smartpos.ui_kit.dialog

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.UiKitDialogBinding
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely
import kotlin.math.roundToInt

class UiKitDialog private constructor() : BaseUiKitDialog<UiKitDialogBinding>() {
    override fun viewBinding() = binding(UiKitDialogBinding::inflate)
    private var closeVisible = false
    override var actionFoldable: (() -> Unit)? = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            this.updateView(this, binding.root, R.layout.ui_kit_dialog)
    }

    override fun setupUI() {
        binding.closeDialog.isVisible = closeVisible
        this@UiKitDialog.title?.let { binding.title.text = it } ?: run { binding.title.isVisible = false }
        this@UiKitDialog.description?.let { binding.description.text = it } ?: run { binding.description.isVisible = false }
        if (closeVisible)
            binding.closeDialog.setOnClickListener { this@UiKitDialog.dismiss() }
        else {
            context?.resources?.getDimension(R.dimen.margin_horizontal)?.roundToInt()?.let { margin ->
                val layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(margin, margin, margin, 0)
                if (this@UiKitDialog.title != null)
                    binding.title.layoutParams = layoutParams
                else if (this@UiKitDialog.description != null)
                    binding.description.layoutParams = layoutParams
            }
        }
        mainBtnText?.let {
            binding.firstAction.text = it
            context.getDrawableSafely(mainBtnDrawableEnd)?.let { endDrawable ->
                binding.firstAction.compoundDrawablePadding = nineDp()
                binding.firstAction.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null)
            }
            binding.firstAction.setOnClickListener { if (dismissOnMainBtnClick) this@UiKitDialog.dismiss(); mainBtnAction?.invoke() }
        } ?: run {
            binding.firstAction.isVisible = false
        }
        secondaryBtnText?.let {
            binding.secondAction.text = it
            binding.secondAction.setOnClickListener { if (dismissOnSecondaryBtnClick) this@UiKitDialog.dismiss(); secondaryBtnAction?.invoke() }
        } ?: run {
            binding.secondAction.isVisible = false
        }
        setupCustomButtons()
    }

    fun withDescription(description: CharSequence) = apply {
        this.description = description
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

    fun withCloseVisible() = apply {
        this.closeVisible = true
    }

    @JvmName("withTitle1")
    fun withTitle(title: CharSequence) = apply {
        this.title = title
    }

    fun withDismissAction(action: () -> Unit) = apply {
        this.dismissAction = action
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

    companion object {
        @JvmStatic
        fun withTitle(title: CharSequence) = UiKitDialog().apply {
            this.title = title
        }

        @JvmStatic
        fun withMainCustomBtn(customizer: CustomBtnCustomizer) = UiKitDialog().apply {
            this.mainCustomBtn = customizer
        }

        @JvmStatic
        fun withMainBtn(mainBtnText: CharSequence, @DrawableRes btnDrawableEnd: Int? = null, action: (() -> Unit)? = null) = UiKitDialog().apply {
            this.mainBtnText = mainBtnText
            this.mainBtnDrawableEnd = btnDrawableEnd
            this.mainBtnAction = action
        }
    }
}