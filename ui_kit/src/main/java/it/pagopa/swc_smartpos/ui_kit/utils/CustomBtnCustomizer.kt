package it.pagopa.swc_smartpos.ui_kit.utils

import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

data class CustomBtnCustomizer(
    val text: CharSequence,
    @DrawableRes val drawable: Int? = null,
    val isStart: Boolean = true,
    /**you need to pass fragment when you're customizing a btn for a dialog*/
    val fragment: Fragment? = null,
    val action: ((Fragment) -> Unit)? = null
) : java.io.Serializable