@file:Suppress("UNUSED")

package it.pagopa.swc_smartpos.ui_kit.input

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.fromColorResToHexadecimalString
import it.pagopa.swc_smartpos.ui_kit.utils.getDrawableSafely
import it.pagopa.swc_smartpos.ui_kit.utils.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class InputText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    enum class InputTypeInputText {
        Password,
        Text,
        Number
    }

    private var textInputLayout: TextInputLayout? = null

    var editText: TextInputEditText? = null
    private var textView: AppCompatTextView? = null
    private var textViewLeft: AppCompatTextView? = null
    private var initialMaxLength: Int = 0
    private var chunk = 4
    private var leftDrawable: Drawable? = null
    private var rightDrawable: Drawable? = null
    private var doCheck = false
    var automaticAction: Boolean = false
    var delayActionDone: Long = 500L
    var actionDone: ((String, Boolean) -> Unit)? = null
    var onAction: ((String, Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.input_field, this)
        textInputLayout = this.findViewById(R.id.textInputLayout)
        editText = this.findViewById(R.id.editTextInputText)
        textView = this.findViewById(R.id.text_input_text)
        textViewLeft = this.findViewById(R.id.text_input_text_left)
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.InputText)
        when (typedArray.getInteger(R.styleable.InputText_inputTypeInputText, 1)) {
            0 -> setInputType(InputTypeInputText.Password)
            1 -> setInputType(InputTypeInputText.Text)
            else -> setInputType(InputTypeInputText.Number)
        }
        typedArray.getString(R.styleable.InputText_hint)?.let { hint -> setHint(hint) }
        typedArray.getString(R.styleable.InputText_left_text)?.let { setLeftText(it) } ?: setLeftText("")
        doCheck = typedArray.getBoolean(R.styleable.InputText_do_check, false)
        leftDrawable = typedArray.getDrawable(R.styleable.InputText_left_drawable)
        rightDrawable = typedArray.getDrawable(R.styleable.InputText_right_drawable)
        chunk = typedArray.getInteger(R.styleable.InputText_chunk, 4)
        setMaxLength(typedArray.getInteger(R.styleable.InputText_max_length, 0))
        typedArray.recycle()
        textInputLayout?.boxStrokeColor = ContextCompat.getColor(context, R.color.primary)
        editText?.doAfterTextChanged {
            textInputLayout?.endIconDrawable = null
            textInputLayout?.boxStrokeColor = ContextCompat.getColor(context, R.color.primary)
            reNormalizeTexts()
            if (chunk != 0) {
                val formattedText = it?.toString()?.replace(" ", "")?.chunked(chunk)?.joinToString(" ")
                if (formattedText != it.toString()) {
                    editText?.setText(formattedText)
                    editText?.setSelection(editText?.length() ?: 0)
                }
            }
            val stringTrimmed = it?.toString()?.replace(" ", "").orEmpty()
            val lengthCtrl = stringTrimmed.length
            if (textView?.isVisible == true)
                textView?.text = "$lengthCtrl/$initialMaxLength"
            val checkAction = checkWithoutUx(lengthCtrl)
            onAction?.invoke(stringTrimmed, checkAction)
            if (lengthCtrl == initialMaxLength && initialMaxLength > 0) {
                val check = checkLength(lengthCtrl)
                if (check && automaticAction) {
                    if (delayActionDone == 0L)
                        actionDone?.invoke(stringTrimmed, true)
                    else {
                        launchInDefaultWithDelay {
                            actionDone?.invoke(stringTrimmed, true)
                        }
                    }
                    context.hideKeyboard()
                }
            }
        }
        editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val stringTrimmed = editText?.text?.toString()?.replace(" ", "")
                val lengthCtrl = stringTrimmed?.length ?: 0
                actionDone?.invoke(stringTrimmed.orEmpty(), if (doCheck) checkLength(lengthCtrl) else true)
                context.hideKeyboard()
            }
            true
        }
        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                initialInputState()
        }
        initialInputState()
    }

    private fun reNormalizeTexts() {
        textView?.setTextColor(ContextCompat.getColor(context, R.color.blue_grey_ultra_medium_dark))
        textViewLeft?.setTextColor(ContextCompat.getColor(context, R.color.blue_grey_ultra_medium_dark))
    }

    private fun initialInputState() {
        val color = R.color.blue_grey_light.fromColorResToHexadecimalString(context)
        val colorState = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_active),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(-android.R.attr.state_focused)
            ),
            intArrayOf(
                Color.parseColor(color),
                Color.parseColor(color),
                Color.parseColor(color)
            )
        )
        textInputLayout?.setBoxStrokeColorStateList(colorState)
        textInputLayout?.startIconDrawable = leftDrawable
        textInputLayout?.endIconDrawable = rightDrawable
        reNormalizeTexts()
    }

    fun setLeftDrawable(drawable: Drawable) {
        leftDrawable = drawable
    }

    fun setRightDrawable(drawable: Drawable) {
        rightDrawable = drawable
    }

    private fun checkWithoutUx(length: Int) = when (length) {
        initialMaxLength -> true
        else -> false
    }

    private fun checkLength(length: Int): Boolean {
        if (doCheck) {
            when (length) {
                0 -> initialInputState()
                initialMaxLength -> {
                    context.getDrawableSafely(R.drawable.success_image)?.let {
                        textInputLayout?.endIconDrawable = it
                    }
                    val successColor = R.color.success.fromColorResToHexadecimalString(context)
                    val colorState = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_active),
                            intArrayOf(android.R.attr.state_focused),
                            intArrayOf(-android.R.attr.state_focused)
                        ),
                        intArrayOf(
                            Color.parseColor(successColor),
                            Color.parseColor(successColor),
                            Color.parseColor(successColor)
                        )
                    )
                    textInputLayout?.setBoxStrokeColorStateList(colorState)
                    reNormalizeTexts()
                    return true
                }
                else -> {
                    context.getDrawableSafely(R.drawable.alert_image)?.let {
                        textInputLayout?.endIconDrawable = it
                    }
                    val errorColor = R.color.error.fromColorResToHexadecimalString(context)
                    val colorState = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_active),
                            intArrayOf(android.R.attr.state_focused),
                            intArrayOf(-android.R.attr.state_focused)
                        ),
                        intArrayOf(
                            Color.parseColor(errorColor),
                            Color.parseColor(errorColor),
                            Color.parseColor(errorColor)
                        )
                    )
                    textInputLayout?.setBoxStrokeColorStateList(colorState)
                    textView?.setTextColor(ContextCompat.getColor(context, R.color.error))
                    textViewLeft?.setTextColor(ContextCompat.getColor(context, R.color.error))
                }
            }
        }
        return false
    }

    fun checkState() {
        doCheck = true
        checkLength(editText?.text?.toString()?.replace(" ", "")?.length ?: 0)
    }

    fun setText(text: CharSequence) {
        editText?.setText(text)
    }

    fun getText() = editText?.text?.toString()?.replace(" ", "")

    fun setTextChunk(chunk: Int) {
        this.chunk = chunk
    }

    fun setInputType(inputType: InputTypeInputText) {
        editText?.inputType = when (inputType) {
            InputTypeInputText.Password -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            InputTypeInputText.Text -> InputType.TYPE_CLASS_TEXT
            InputTypeInputText.Number -> InputType.TYPE_CLASS_NUMBER
        }
    }

    fun setHint(hint: CharSequence) {
        textInputLayout?.hint = hint
    }

    fun setLeftText(text: CharSequence) {
        textViewLeft?.text = text
        textViewLeft?.isVisible = text != ""
    }

    fun setMaxLength(length: Int) {
        initialMaxLength = length
        if (length > 0) {
            textView?.isVisible = true
            val divider = if (chunk == 0) 1 else chunk
            editText?.filters = arrayOf(InputFilter.LengthFilter(floor(length.toFloat() / divider.toFloat()).roundToInt() + if (chunk > 0) length else 0))
            textView?.text = "0/$length"
        } else
            textView?.isVisible = false
    }

    fun setFocus(giveIt: Boolean) {
        if (giveIt) {
            editText?.requestFocus()
            if ((editText?.text?.length ?: 0) > 0)
                editText?.setSelection(editText?.length() ?: 0)
            launchInDefaultWithDelay {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        } else
            editText?.clearFocus()
    }


    private inline fun launchInDefaultWithDelay(crossinline block: (CoroutineScope) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delayActionDone)
            block.invoke(this)
        }
    }

    fun inputTypeOnlyAlphaNumeric() {
        editText?.keyListener = DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
        editText?.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }
}