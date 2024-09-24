package it.pagopa.swc_smartpos.ui_kit.progress

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.withResumed
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.utils.AnimationEndListener
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimatedProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mBackgroundViewDrawable: Drawable? = null

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AnimatedProgress)
        when (typedArray.getInteger(R.styleable.AnimatedProgress_kind_of_progress, -1)) {
            0 -> mBackgroundViewDrawable = getDrawable(R.drawable.rounded_info_dark_filled_4_dp)
            1 -> mBackgroundViewDrawable = getDrawable(R.drawable.rounded_error_dark_filled_4_dp)
            2 -> mBackgroundViewDrawable = getDrawable(R.drawable.rounded_warning_dark_filled_4_dp)
            3 -> mBackgroundViewDrawable = getDrawable(R.drawable.rounded_success_dark_filled_4_dp)
        }
        typedArray.recycle()
        buildLayout()
    }

    private fun buildLayout() {
        getDrawable(R.drawable.rounded_white_opacity_60_4_dp)?.let { this.background = it }
        val lazyLoadingWidth = context.resources.getDimension(R.dimen.animated_loading_width)
        val viewParam = LayoutParams(lazyLoadingWidth.toInt(), LayoutParams.MATCH_PARENT)
        val view = View(context).apply {
            mBackgroundViewDrawable?.let { this.background = it }
            layoutParams = viewParam
        }
        this.addView(view)
        val animIn = AnimationUtils.loadAnimation(context, R.anim.outside_left_to_in_right)
        val anim = AnimationUtils.loadAnimation(context, R.anim.left_to_right)
        CoroutineScope(Dispatchers.Main).launch {
            view.repeatAnimation(animIn, anim)
        }
    }

    fun setKindOfProgress(style: Style) {
        mBackgroundViewDrawable = when (style) {
            Style.Info -> getDrawable(R.drawable.rounded_info_dark_filled_4_dp)
            Style.Error -> getDrawable(R.drawable.rounded_error_dark_filled_4_dp)
            Style.Warning -> getDrawable(R.drawable.rounded_warning_dark_filled_4_dp)
            Style.Success -> getDrawable(R.drawable.rounded_success_dark_filled_4_dp)
        }
        buildLayout()
        invalidate()
    }

    private suspend fun View.repeatAnimation(animIn: Animation, animation: Animation) {
        this.startAnimation(animIn)
        findViewTreeLifecycleOwner()?.withResumed {
            animIn.setAnimationListener(AnimationEndListener {
                this@repeatAnimation.startAnimation(animation)
            })
            animation.setAnimationListener(AnimationEndListener {
                this@repeatAnimation.startAnimation(animIn)
            })
        }
    }

    private fun getDrawable(id: Int) = ContextCompat.getDrawable(context, id)
}