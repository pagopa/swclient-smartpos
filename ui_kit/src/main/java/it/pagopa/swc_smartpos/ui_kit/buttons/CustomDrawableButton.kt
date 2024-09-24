package it.pagopa.swc_smartpos.ui_kit.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPxWith
import kotlin.math.roundToInt

class CustomDrawableButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var ivChild: AppCompatImageView
    private var textView: AppCompatTextView
    private var iconGravity: IconGravity
    private var llHorizontal: LinearLayoutCompat
    private var iconPadding: Float

    init {
        val isBigTerminal = context.resources.getBoolean(R.bool.isBigTerminal)
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomDrawableButton)
        iconGravity = IconGravity.fromInt(typedArray.getInteger(R.styleable.CustomDrawableButton_drawableGravity, 0))
        val buttonIcon = typedArray.getDrawable(R.styleable.CustomDrawableButton_button_icon)
        val text = typedArray.getString(R.styleable.CustomDrawableButton_button_text)
        val textColor = typedArray.getColor(R.styleable.CustomDrawableButton_text_color, ContextCompat.getColor(context, R.color.white))
        val textFont = typedArray.getResourceId(R.styleable.CustomDrawableButton_text_font, R.font.readex_pro)
        iconPadding = typedArray.getDimension(R.styleable.CustomDrawableButton_icon_padding, if (isBigTerminal) 15f else 10f dpToPxWith context)
        typedArray.recycle()
        val param = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        val paddingHorizontal = context.resources.getDimension(R.dimen.button_padding_horizontal).roundToInt()
        val paddingVertical = context.resources.getDimension(R.dimen.drawable_button_padding_vertical).roundToInt()
        param.setMargins(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        llHorizontal = LinearLayoutCompat(context).apply {
            layoutParams = param
            id = View.generateViewId()
            orientation = LinearLayoutCompat.HORIZONTAL
        }
        val llHorizontalParam = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        textView = AppCompatTextView(context, attrs).apply {
            id = View.generateViewId()
            setText(text)
            if (!isInEditMode) {
                typeface = ResourcesCompat.getFont(context, textFont)
            }
            setTextColor(textColor)
            val padding = 10
            setPadding(if (iconGravity.isStart()) 0 else padding, 0, if (iconGravity.isStart()) padding else 0, 0)
            layoutParams = llHorizontalParam
            background = null
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        ivChild = AppCompatImageView(context).apply {
            id = View.generateViewId()
            setImageDrawable(buttonIcon)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        setLLChildren()
        this.addView(llHorizontal)
        this.contentDescription = text
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }

    fun setTextColor(@ColorInt color: Int) {
        textView.setTextColor(color)
    }

    private fun setLLChildren() {
        if (iconGravity == IconGravity.Start) {
            llHorizontal.addView(ivChild)
            llHorizontal.addView(textView)
        } else {
            llHorizontal.addView(textView)
            llHorizontal.addView(ivChild)
        }
        val ivParam = LinearLayoutCompat.LayoutParams(
            if (!isInEditMode)
                context.resources.getDimension(R.dimen.drawable_button_image).roundToInt()
            else
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            if (!isInEditMode)
                context.resources.getDimension(R.dimen.drawable_button_image).roundToInt()
            else
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        val padding = iconPadding.roundToInt()
        ivParam.setMargins(
            if (iconGravity.isStart()) 0 else padding,
            0,
            if (iconGravity.isStart()) padding else 0,
            0
        )
        ivChild.layoutParams = ivParam
    }

    enum class IconGravity(val value: Int) {
        Start(0),
        End(1);

        fun isStart() = this == Start

        companion object {
            fun fromInt(int: Int): IconGravity = if (int == 0) Start else End
            fun fromBoolean(isStart: Boolean) = if (isStart) Start else End
        }
    }

    fun setIv(iv: Drawable?, newIconGravity: IconGravity? = null) {
        if (newIconGravity != null) {
            iconGravity = newIconGravity
            llHorizontal.removeAllViews()
            setLLChildren()
        }
        if (iv == null) {
            ivChild.visibility = View.GONE
            textView.setPadding(10, 0, 10, 0)
        } else {
            ivChild.visibility = View.VISIBLE
            textView.setPadding(if (iconGravity.isStart()) 0 else 10, 0, if (iconGravity.isStart()) 10 else 0, 0)
            ivChild.setImageDrawable(iv)
        }
    }
}