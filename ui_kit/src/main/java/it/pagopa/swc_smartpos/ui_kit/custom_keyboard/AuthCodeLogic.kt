package it.pagopa.swc_smartpos.ui_kit.custom_keyboard

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx

internal class AuthCodeLogic(private val father: LinearLayoutCompat? = null) {
    fun addCodeView(context: Context, title: String): Array<FrameLayout>? {
        father.addTitle(context, title, true)
        return father?.addAuthCode(context)
    }

    private fun LinearLayoutCompat.addAuthCode(context: Context): Array<FrameLayout> {
        val res = context.resources
        val size = context.dpToPx(
            if (isInEditMode) 40f
            else res.getDimension(R.dimen.pin_ball)
        )
        val frameLayouts = ArrayList<FrameLayout>().apply {
            for (i in 0 until 6)
                add(FrameLayout(context).apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(size, LinearLayoutCompat.LayoutParams.MATCH_PARENT)
                    isVisible = false
                })
        }
        this.addView(LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, size).also {
                it.gravity = Gravity.CENTER
                it.setMargins(0, context dpToPx 16f, 0, 0)
            }
            frameLayouts.forEach { this.addView(it) }
        })
        return frameLayouts.toTypedArray()
    }

    private fun FrameLayout.convertToPin(width: Int) {
        background = null
        addView(AppCompatImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT)
            setImageResource(R.drawable.pinnumber_in)
            scaleType = ImageView.ScaleType.CENTER_CROP
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        })
    }

    private fun FrameLayout.convertToNotEdited() {
        isVisible = false
        removeAllViews()
    }

    fun logic(context: Context, isInEditMode: Boolean, array: Array<FrameLayout>?, value: String) {
        for (it in 0..5) {
            if (value.length > it) {
                array?.get(it)?.also {
                    it.convertToPin(
                        context.dpToPx(
                            if (isInEditMode) 40f
                            else context.resources.getDimension(R.dimen.pin_ball)
                        )
                    )
                    it.isVisible = true
                }
            } else {
                array?.get(it)?.convertToNotEdited()
            }
        }
    }
}