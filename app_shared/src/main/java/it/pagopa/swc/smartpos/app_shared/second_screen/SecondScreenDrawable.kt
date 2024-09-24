package it.pagopa.swc.smartpos.app_shared.second_screen

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import it.pagopa.swc_smartpos.ui_kit.utils.fromColorResToHexadecimalString


class SecondScreenDrawable private constructor() {
    private var textDrawablesArray: ArrayList<TextDrawable>? = null
    private var imageDrawablesArray: ArrayList<ImageDrawable>? = null

    @ColorRes
    private var background: Int? = null

    @DrawableRes
    private var backgroundDrawable: Int? = null

    fun construct(context: Context, listSize: Int? = null): Drawable {
        val listDrawables = ArrayList<Drawable>()
        background?.let {
            val color = Color.parseColor(it.fromColorResToHexadecimalString(context))
            listDrawables.add(GradientDrawable().apply {
                colors = intArrayOf(color, color)
            })
        }
        backgroundDrawable?.let {
            ContextCompat.getDrawable(context, it)?.let { drawable ->
                listDrawables.add(drawable)
            }
        }
        imageDrawablesArray?.forEach {
            listDrawables.add(it.build(listSize ?: imageDrawablesArray!!.size))
        }
        textDrawablesArray?.forEach {
            listDrawables.add(it.build(context, listSize ?: textDrawablesArray!!.size))
        }
        return LayerDrawable(listDrawables.toTypedArray())
    }

    fun constructAsLinearLayout(context: Context, listSize: Int? = null, howMuchWhenIndentTexts: Int = 20): Drawable {
        val listDrawables = ArrayList<Drawable>()
        background?.let {
            val color = Color.parseColor(it.fromColorResToHexadecimalString(context))
            listDrawables.add(GradientDrawable().apply {
                colors = intArrayOf(color, color)
            })
        }
        backgroundDrawable?.let {
            ContextCompat.getDrawable(context, it)?.let { drawable ->
                listDrawables.add(drawable)
            }
        }
        val listSizeHere = listSize ?: ((imageDrawablesArray?.size ?: 0) + (textDrawablesArray?.size ?: 0))
        imageDrawablesArray?.forEach {
            it.withListSizeLL(listSizeHere)
            listDrawables.add(it.buildAsLinearLayout())
        }
        textDrawablesArray?.forEach {
            it.withListSizeLL(listSizeHere)
            listDrawables.add(it.buildAsLinearLayout(context,howMuchWhenIndentTexts))//do it for image too
        }
        return LayerDrawable(listDrawables.toTypedArray())
    }

    @JvmName("withBackGroundColor1")
    fun withBackGroundColor(@ColorRes color: Int) = apply {
        background = color
    }

    @JvmName("withTextDrawables1")
    fun withTextDrawables(array: ArrayList<TextDrawable>) = apply {
        textDrawablesArray = array
    }

    @JvmName("withBackGroundDrawable1")
    fun withBackGroundDrawable(@DrawableRes drawable: Int) = apply {
        backgroundDrawable = drawable
    }

    @JvmName("withImageDrawables1")
    fun withImageDrawables(array: ArrayList<ImageDrawable>) = apply {
        imageDrawablesArray = array
    }

    companion object {
        fun withBackGroundDrawable(@DrawableRes drawable: Int) = SecondScreenDrawable().apply {
            backgroundDrawable = drawable
        }

        fun withBackGroundColor(@ColorRes color: Int) = SecondScreenDrawable().apply {
            background = color
        }

        fun withImageDrawables(array: ArrayList<ImageDrawable>) = SecondScreenDrawable().apply {
            imageDrawablesArray = array
        }

        fun withTextDrawables(array: ArrayList<TextDrawable>) = SecondScreenDrawable().apply {
            textDrawablesArray = array
        }
    }
}