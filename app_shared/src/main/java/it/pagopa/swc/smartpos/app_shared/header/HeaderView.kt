package it.pagopa.swc.smartpos.app_shared.header

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import it.pagopa.swc.smartpos.app_shared.R
import it.pagopa.swc.smartpos.app_shared.databinding.AppHeaderBinding
import it.pagopa.swc_smartpos.ui_kit.utils.invisibleWithAccessibility
import it.pagopa.swc_smartpos.ui_kit.utils.visibleWithAccessibility

data class HeaderView(
    val imageSx: HeaderElement?,
    val text: HeaderString?,
    val imageDx: HeaderElement?,
    @ColorRes val background: Int? = null
) : java.io.Serializable {
    data class HeaderString(
        @StringRes val string: Int,
        @ColorRes val textColor: Int = R.color.white
    ) : java.io.Serializable

    data class HeaderElement(val image: Int, val action: () -> Unit) : java.io.Serializable

    fun bind(context: Context?, binding: AppHeaderBinding) {
        background?.let { backGroundPassed ->
            context?.let { binding.root.background = ContextCompat.getDrawable(it, backGroundPassed) }
        }
        text?.let { headerString ->
            binding.headerTitle.isVisible = true
            binding.headerTitle.text = context?.resources?.getString(headerString.string).orEmpty()
            context?.let { binding.headerTitle.setTextColor(ContextCompat.getColor(it, headerString.textColor)) }
        } ?: run {
            binding.headerTitle.isVisible = false
        }
        imageSx?.let { element ->
            binding.ivBack.visibleWithAccessibility()
            binding.ivBack.setImageResource(element.image)
            binding.ivBack.setOnClickListener { element.action.invoke() }
        } ?: run {
            binding.ivBack.invisibleWithAccessibility()
        }
        imageDx?.let { element ->
            binding.ivHome.visibleWithAccessibility()
            binding.ivHome.setImageResource(element.image)
            binding.ivHome.setOnClickListener { element.action.invoke() }
        } ?: run {
            binding.ivHome.invisibleWithAccessibility()
        }
    }
}