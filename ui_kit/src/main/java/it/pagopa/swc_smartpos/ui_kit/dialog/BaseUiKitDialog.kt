package it.pagopa.swc_smartpos.ui_kit.dialog

import android.content.DialogInterface
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import it.pagopa.swc_smart_pos.ui_kit.databinding.UiKitDialogBinding
import it.pagopa.swc_smart_pos.ui_kit.databinding.UiKitStyledDialogBinding
import it.pagopa.swc_smartpos.ui_kit.buttons.CustomDrawableButton
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely

abstract class BaseUiKitDialog<T : ViewBinding> : BaseDataBindingDialog<T>() {
    var mainCustomBtn: CustomBtnCustomizer? = null
    var secondaryCustomBtn: CustomBtnCustomizer? = null
    var mainBtnText: CharSequence? = null
    var mainBtnAction: (() -> Unit)? = null
    var secondaryBtnText: CharSequence? = null
    var secondaryBtnAction: (() -> Unit)? = null
    var dismissAction: (() -> Unit)? = null
    var title: CharSequence? = null
    var description: CharSequence? = null
    var dismissOnMainBtnClick = true
    var dismissOnSecondaryBtnClick = true

    @DrawableRes
    var mainBtnDrawableEnd: Int? = null

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        this.dismissAction?.invoke()
    }

    fun nineDp() = context?.dpToPx(9f) ?: 0

    private fun setupCustomButton(customizer: CustomBtnCustomizer?, firstButton: Boolean) {
        val button = if (binding is UiKitDialogBinding) {
            if (firstButton)
                (binding as UiKitDialogBinding).firstActionCustomButton
            else
                (binding as UiKitDialogBinding).secondActionCustomButton
        } else {
            if (firstButton)
                (binding as UiKitStyledDialogBinding).firstActionCustomButton
            else
                (binding as UiKitStyledDialogBinding).secondActionCustomButton
        }
        if (customizer == null) {
            button.isVisible = false; return
        }
        button.isVisible = true
        context.getDrawableSafely(customizer.drawable)?.let { drawable ->
            button.setIv(
                drawable, if (customizer.isStart)
                    CustomDrawableButton.IconGravity.Start
                else
                    CustomDrawableButton.IconGravity.End
            )
        }
        button.setText(customizer.text)
        button.setOnClickListener {
            this.dismiss()
            customizer.action?.invoke(customizer.fragment ?: this)
        }
    }

    fun setupCustomButtons() {
        setupCustomButton(mainCustomBtn, true)
        setupCustomButton(secondaryCustomBtn, false)
    }
}