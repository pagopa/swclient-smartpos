package it.pagopa.swc_smartpos.ui_kit.buttons

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx


class CustomLoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var isLoading = false
    private lateinit var button: AppCompatButton
    private lateinit var myPb: ProgressBar
    private var lazyLoading: LottieAnimationView? = null
    private var text: String? = ""
    val isEnabledLiveData = MutableLiveData(true)
    private val observer = Observer<Boolean> {
        this.isEnabled = it
        if (::button.isInitialized)
            button.isEnabled = it
        if (::myPb.isInitialized)
            myPb.isEnabled = it
    }

    init {
        val isBigTerminal = context.resources.getBoolean(R.bool.isBigTerminal)
        this.setPadding(0, 0, 0, 0)
        val param = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        button = AppCompatButton(context, attrs, defStyleAttr).apply {
            layoutParams = param
            id = View.generateViewId()
        }
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomLoadingButton)
        text = typedArray.getString(R.styleable.CustomLoadingButton_text).orEmpty()
        button.text = text.orEmpty()
        typedArray.recycle()
        val progressWidth = context.resources.getDimension(R.dimen.progress_width)
        val progressHeight = context.resources.getDimension(R.dimen.progress_height)
        val pbParam = LayoutParams(progressWidth.toInt(), progressHeight.toInt(), Gravity.CENTER)
        myPb = ProgressBar(context, null, android.R.attr.progressBarStyle).apply {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            max = 100
            if (isBigTerminal) {
                val marginsHorizontal = context dpToPx 48f
                val marginsVertical = context dpToPx 11f
                pbParam.setMargins(marginsHorizontal, marginsVertical + 1, marginsHorizontal, marginsVertical)
            }
            layoutParams = pbParam
            isIndeterminate = true
        }

        myPb.indeterminateDrawable = progressDrawable(background)
        this.addView(button)
        this.addView(myPb)
        if (!isInEditMode) {
            val lazyLoadingWidth = context.resources.getDimension(R.dimen.lazy_loading_width)
            val lazyLoadingHeight = context.resources.getDimension(R.dimen.lazy_loading_heigth)
            val lottieParam = LayoutParams(lazyLoadingWidth.toInt(), lazyLoadingHeight.toInt() + 1, Gravity.CENTER)
            lazyLoading = LottieAnimationView(context).apply {
                repeatCount = ValueAnimator.INFINITE
                this.setAnimation(R.raw.lazy_load_shimmer)
                scaleType = ImageView.ScaleType.FIT_XY
                playAnimation()
                val paddingHorizontal = context.resources?.getDimension(R.dimen.button_padding_horizontal)?.toInt() ?: 0
                val paddingVertical = if (isBigTerminal) context.resources?.getDimension(R.dimen.button_padding_vertical)?.toInt() ?: 0 else 0
                lottieParam.setMargins(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
                layoutParams = lottieParam
            }
            this.addView(lazyLoading)
            lazyLoading?.isVisible = false
        }
        button.setOnClickListener { this.performClick() }
        myPb.setOnClickListener { this.performClick() }
        myPb.isVisible = false
        button.background = null
        this.isClickable = false
    }

    fun setButtonText(text: CharSequence) {
        button.text = text
    }

    fun getInsideButton(): AppCompatButton = button

    private fun getDrawable(id: Int) = ContextCompat.getDrawable(context, id)
    private fun progressDrawable(backgroundDrawable: Drawable): Drawable? {
        return when (backgroundDrawable.constantState) {
            getDrawable(R.drawable.rounded_primary_filled_8dp)?.constantState,
            getDrawable(R.drawable.rounded_white_primary_border_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_white_primary)
            getDrawable(R.drawable.rounded_success_light_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_success)
            getDrawable(R.drawable.rounded_success_dark_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_white_success)
            getDrawable(R.drawable.rounded_info_light_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_info)
            getDrawable(R.drawable.rounded_info_dark_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_white_info)
            getDrawable(R.drawable.rounded_error_light_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_error)
            getDrawable(R.drawable.rounded_error_dark_filled_8dp)?.constantState -> getDrawable(R.drawable.gradient_progress_white_error)
            else -> getDrawable(R.drawable.gradient_progress_primary)
        }
    }

    fun showLoading(loading: Boolean) {
        lazyLoading?.isVisible = false
        isLoading = loading
        if (loading) {
            myPb.isVisible = true
            button.isVisible = false
        } else {
            button.isVisible = true
            myPb.isVisible = false
        }
    }

    fun showLazyLoading(showIt: Boolean) {
        isLoading = showIt
        if (showIt) {
            button.isVisible = false
            myPb.isVisible = false
            lazyLoading?.isVisible = true
        } else {
            button.isVisible = true
            myPb.isVisible = false
            lazyLoading?.isVisible = false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isEnabledLiveData.observeForever(observer)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isEnabledLiveData.removeObserver(observer)
    }
}