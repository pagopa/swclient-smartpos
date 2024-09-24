package it.pagopa.swc_smartpos.ui_kit.custom_keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import kotlin.math.roundToInt

class CustomKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var title: String
    private var isForPin = false

    @VisibleForTesting
    val textWritten = MutableLiveData("")
    private var importTextView: AppCompatTextView? = null
    private var flForPin: Array<FrameLayout>? = null
    var doAfterTextChanged: ((String) -> Unit)? = null
    private val observer = Observer<String> {
        if (!isForPin)
            ImportLogic().setImportText(importTextView, it)
        else
            AuthCodeLogic().logic(context, isInEditMode, flForPin, it)
        doAfterTextChanged?.invoke(it)
    }

    fun clearText() {
        textWritten.postValue("")
    }

    private fun updateWrittenText(number: String) {
        textWritten.value?.let {
            if (it.length < if (isForPin) 6 else 8)
                textWritten.postValue(textWritten.value + number)
        }
    }

    private fun cancelLogic() {
        textWritten.value?.let {
            if (it.isNotEmpty())
                textWritten.postValue(it.substring(0, it.length - 1))
        }
    }

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomKeyboard)
        isForPin = typedArray.getBoolean(R.styleable.CustomKeyboard_is_pin, true)
        title = typedArray.getString(R.styleable.CustomKeyboard_title) ?: getBasicTitle()
        typedArray.recycle()
        if (isForPin)
            this.background = ContextCompat.getDrawable(context, R.color.grey_light)
        this.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val margins = if (!isInEditMode)
            context dpToPx context.resources.getInteger(R.integer.margin_horizontal_size).toFloat()
        else
            context dpToPx 24f
        val marginBottom = if (!isInEditMode)
            context dpToPx context.resources.getInteger(R.integer.margin_bottom_keyboard).toFloat()
        else
            context dpToPx 32f
        val llParam = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        val father = LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.VERTICAL
            layoutParams = llParam.also {
                it.gravity = Gravity.CENTER
            }
            setPadding(0, margins, 0, marginBottom)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        if (!isForPin)
            importTextView = ImportLogic(father).addImportView(context, title)
        else
            flForPin = AuthCodeLogic(father).addCodeView(context, title)
        father.addViewWithWeight1()
        father.addKeyboard(context)
        father.addViewWithWeight1()
        this.addView(father)
    }

    private fun getBasicTitle(): String {
        return if (isForPin) {
            if (!isInEditMode)
                context.resources.getString(R.string.insert_auth_code)
            else
                "Inserisci codice di autorizzazione"
        } else {
            if (!isInEditMode)
                context.resources.getString(R.string.amount)
            else
                "IMPORTO"
        }
    }

    private fun LinearLayoutCompat.addViewWithWeight1() {
        this.addView(View(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(1, 0, 0.5f)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        })
    }

    private fun LinearLayoutCompat.addKeyboard(context: Context) {
        val param = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
        this.addView(LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.VERTICAL
            layoutParams = param.also {
                it.gravity = Gravity.CENTER
            }
            this.addView(context oneChildWithArray arrayOf("1", "2", "3"))
            this.addView(context.oneViewForVerticalLL())
            this.addView(context oneChildWithArray arrayOf("4", "5", "6"))
            this.addView(context.oneViewForVerticalLL())
            this.addView(context oneChildWithArray arrayOf("7", "8", "9"))
            this.addView(context.oneViewForVerticalLL())
            this.addView(context.oneChildWithCancel(arrayOf("00", "0"), isForPin))
        })
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun Context.oneViewForVerticalLL() = View(this).apply {
        val size = this@oneViewForVerticalLL dpToPx 6f
        layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun Context.oneViewWithHorizontalSpace() = View(this).apply {
        layoutParams = LinearLayoutCompat.LayoutParams(0, 0, 1f)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun Context.oneViewWithMainSize() = View(this).apply {
        val size = this@oneViewWithMainSize.resources.getDimension(R.dimen.keyboard_number_size).roundToInt()
        layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun Context.addThisNumber(number: String, isPin: Boolean = false) = FrameLayout(this).apply {
        val size = this@addThisNumber.resources.getDimension(R.dimen.keyboard_number_size).roundToInt()
        layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        background = AppCompatResources.getDrawable(this@addThisNumber, if (isForPin) R.drawable.ellipse_pin else R.drawable.ellipse_keyboard)
        isClickable = true
        isFocusable = true
        contentDescription = number
        setOnClickListener { view ->
            view.clickAnimation()
            textWritten.value?.toIntOrNull()?.let {
                if (isPin)
                    updateWrittenText(number)
                else {
                    if (number.toInt() > 0)
                        updateWrittenText(number)
                    else if (it > 0)
                        updateWrittenText(number)
                }
            } ?: run {
                if (textWritten.value.isNullOrEmpty()) {
                    if (isPin)
                        updateWrittenText(number)
                    else {
                        if (number.toInt() > 0)
                            updateWrittenText(number)
                    }
                }
            }
        }
    }.also {
        it.addView(AppCompatTextView(this).apply {
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.titillium_web_regular)
            setTextColor(ContextCompat.getColor(this@addThisNumber, R.color.primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, this@addThisNumber.resources.getInteger(R.integer.keyboard_number_text_size).toFloat())
            text = number
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        })
    }

    private infix fun Context.oneChildWithArray(array: Array<String>): LinearLayoutCompat {
        return LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, this@oneChildWithArray dpToPx 0f, 0.21f)
        }.also {
            it.addView(this.oneViewWithHorizontalSpace())
            it.addView(this.addThisNumber(array[0]))
            it.addView(this.oneViewWithHorizontalSpace())
            it.addView(this.addThisNumber(array[1]))
            it.addView(this.oneViewWithHorizontalSpace())
            it.addView(this.addThisNumber(array[2]))
            it.addView(this.oneViewWithHorizontalSpace())
        }
    }

    private fun Context.addCancel() = FrameLayout(this).apply {
        val size = if (!isInEditMode)
            this@addCancel.resources.getDimension(R.dimen.keyboard_number_size).roundToInt()
        else
            this@addCancel dpToPx 60f
        layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        isClickable = true
        isFocusable = true
        contentDescription = this@addCancel.resources.getString(R.string.cancel)
        setPadding(this@addCancel dpToPx 16f)
        setOnClickListener { view ->
            view.clickAnimation()
            cancelLogic()
        }
    }.also {
        it.addView(AppCompatImageView(this).apply {
            setImageResource(R.drawable.keyboard_cancel)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        })
    }

    private fun View.clickAnimation() {
        this.alpha = 0.3f
        this.animate().alpha(1f).setDuration(500).start()
    }

    private fun Context.oneChildWithCancel(array: Array<String>, isPin: Boolean): LinearLayoutCompat {
        return LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, this@oneChildWithCancel dpToPx 0f, 0.21f)
        }.also {
            if (isPin) {
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.oneViewWithMainSize())
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.addThisNumber(array[1], true))
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.addCancel())
                it.addView(this.oneViewWithHorizontalSpace())
            } else {
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.addThisNumber(array[0]))
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.addThisNumber(array[1]))
                it.addView(this.oneViewWithHorizontalSpace())
                it.addView(this.addCancel())
                it.addView(this.oneViewWithHorizontalSpace())
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        textWritten.observeForever(observer)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        textWritten.removeObserver(observer)
    }
}