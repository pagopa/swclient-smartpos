package it.pagopa.swc_smartpos.ui_kit.custom_keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import kotlin.math.roundToInt

internal class ImportLogic(private val father: LinearLayoutCompat? = null) {
    private fun LinearLayoutCompat.LayoutParams.forHorizontalRow(context: Context, isInEditMode: Boolean) = this.also {
        val marginTop = if (!isInEditMode)
            context.resources.getDimension(R.dimen.drawable_button_padding_vertical).roundToInt()
        else
            context dpToPx 11f
        val marginBottom = if (!isInEditMode)
            context.resources.getDimension(R.dimen.margin_horizontal).roundToInt()
        else
            context dpToPx 24f
        it.setMargins(
            0,
            marginTop,
            0,
            marginBottom
        )
        it.gravity = Gravity.CENTER
    }

    @SuppressLint("SetTextI18n")
    fun addImportView(context: Context, title: String): AppCompatTextView {
        val importTextView = AppCompatTextView(context)
        father.addTitle(context, title, false)
        father?.addView(LinearLayoutCompat(context).apply {
            val param = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayoutCompat.HORIZONTAL
            layoutParams = param.forHorizontalRow(context, isInEditMode)
            val tvParam = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            importTextView.layoutParams = tvParam
            importTextView.typeface = ResourcesCompat.getFont(context, R.font.readex_pro)
            importTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
            importTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP, if (!isInEditMode)
                    context.resources.getInteger(R.integer.h1_hero_size).toFloat()
                else
                    32f
            )
            importTextView.text = "0,00"
            addView(importTextView)
            addView(View(context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(context dpToPx 4f, context dpToPx 1f)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            })
            val tv2 = AppCompatTextView(context)
            tv2.layoutParams = tvParam
            tv2.typeface = ResourcesCompat.getFont(context, R.font.readex_pro)
            tv2.setTextColor(ContextCompat.getColor(context, R.color.black))
            tv2.setTextSize(
                TypedValue.COMPLEX_UNIT_SP, if (!isInEditMode)
                    context.resources.getInteger(R.integer.h6_size).toFloat()
                else
                    16f
            )
            tv2.text = if (!isInEditMode) context.resources.getString(R.string.currency) else "€"
            addView(tv2)
        })
        return importTextView
    }

    @SuppressLint("SetTextI18n")
    fun setImportText(tv: AppCompatTextView?, value: String) {
        var toChange = true
        value.toIntOrNull()?.let {
            if (it <= 0) {
                toChange = false
                tv?.text = "0,00"
            }
        }
        if (toChange) {
            tv?.text = when (value.length) {
                0 -> "0,00"
                1 -> "0,0$value"
                2 -> "0,$value"
                3 -> value.first() + "," + value.substring(1, value.length)
                else -> value.chunked(value.length - 2).joinToString(",")
            }
        }
    }
}