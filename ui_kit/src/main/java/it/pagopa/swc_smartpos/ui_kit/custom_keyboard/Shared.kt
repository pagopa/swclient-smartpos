package it.pagopa.swc_smartpos.ui_kit.custom_keyboard

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import it.pagopa.swc_smart_pos.ui_kit.R

internal fun LinearLayoutCompat?.addTitle(context: Context, title: String, isPin: Boolean) {
    this?.addView(AppCompatTextView(context).apply {
        val param = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
        layoutParams = param.also {
            it.gravity = Gravity.CENTER
        }
        text = title
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setTextSize(
            TypedValue.COMPLEX_UNIT_SP, if (!isInEditMode) {
                this.resources.getInteger(if(isPin)
                    R.integer.h6_size
                else
                    R.integer.amount_text_size).toFloat()
            } else {
                if(isPin)
                    16f
                else
                    12f
            }
        )
        typeface = ResourcesCompat.getFont(context, if(isPin) R.font.titillium_web_regular else R.font.readex_pro)
    })
}