package it.pagopa.swc_smartpos.ui_kit.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.buttons.CustomLoadingButton
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import kotlin.math.roundToInt

/**
 * Triggers a snackBar message when the value contained by snackBarTaskMessageLiveEvent is modified.
 */
fun View.setupUiKitToast(
    lifecycleOwner: LifecycleOwner,
    mutableLiveData: MutableLiveData<UiKitToast?>,
    event: LiveData<UiKitToast?>
) {
    event.observe(lifecycleOwner) {
        if (it != null) {
            val snackBar = Snackbar.make(this, it.text, it.timeLength)
            val vg = (snackBar.view as ViewGroup)
            vg.removeAllViews()
            val viewToAdd = this.context.inflateUiKitToastAs(it.value, vg)
            val marginHorizontal = context.resources.getDimension(R.dimen.margin_horizontal).roundToInt()
            val snackView = snackBar.view
            snackView.setPadding(marginHorizontal - snackView.marginStart, 0, marginHorizontal - snackView.marginEnd, marginHorizontal - snackView.marginBottom)
            snackView.elevation = 0f
            snackBar.setBackgroundTint(
                ContextCompat.getColor(
                    this.context,
                    android.R.color.transparent
                )
            )
            val textView = viewToAdd.findViewById<AppCompatTextView?>(R.id.toast_text)
            val image = viewToAdd.findViewById<AppCompatImageView?>(R.id.toast_image)
            if (!it.showImage)
                image?.isVisible = false
            textView?.text = it.text
            snackBar.show()
            mutableLiveData.postValue(null)
        }
    }
}

fun CustomLoadingButton.disablePrimaryButton(context: Context?) {
    context?.getDrawableSafely(R.drawable.rounded_blue_gry_light_filled_8dp)?.let { bck -> this.background = bck }
    context?.getColorSafely(R.color.blue_grey_ultra_medium_dark)?.let { txtColor -> this.getInsideButton().setTextColor(txtColor) }
    this.alpha = .5f
    this.isEnabledLiveData.value = false
}

fun CustomLoadingButton.enablePrimaryButton(context: Context?) {
    context?.getDrawableSafely(R.drawable.rounded_primary_filled_8dp)?.let { bck -> this.background = bck }
    context?.getColorSafely(R.color.white)?.let { txtColor -> this.getInsideButton().setTextColor(txtColor) }
    this.alpha = 1f
    this.isEnabledLiveData.value = true
}

fun AppCompatButton.disablePrimaryButton(context: Context?) {
    context?.getDrawableSafely(R.drawable.rounded_blue_gry_light_filled_8dp)?.let { bck -> this.background = bck }
    context?.getColorSafely(R.color.blue_grey_ultra_medium_dark)?.let { txtColor -> this.setTextColor(txtColor) }
    this.alpha = .5f
    this.isEnabled = false
}

fun AppCompatButton.enablePrimaryButton(context: Context?) {
    context?.getDrawableSafely(R.drawable.rounded_primary_filled_8dp)?.let { bck -> this.background = bck }
    context?.getColorSafely(R.color.white)?.let { txtColor -> this.setTextColor(txtColor) }
    this.alpha = 1f
    this.isEnabled = true
}

@SuppressLint("InflateParams")
private fun Context?.inflateUiKitToastAs(value: UiKitToast.Value, viewRoot: ViewGroup): View {
    return when (value) {
        UiKitToast.Value.Generic -> LayoutInflater.from(this).inflate(R.layout.generic_toast, viewRoot)
        UiKitToast.Value.Success -> LayoutInflater.from(this).inflate(R.layout.success_toast, viewRoot)
        UiKitToast.Value.Info -> LayoutInflater.from(this).inflate(R.layout.info_toast, viewRoot)
        UiKitToast.Value.Warning -> LayoutInflater.from(this).inflate(R.layout.warning_toast, viewRoot)
        UiKitToast.Value.Error -> LayoutInflater.from(this).inflate(R.layout.error_toast, viewRoot)
    }
}

fun Context?.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context?.getDrawableSafely(@DrawableRes value: Int?): Drawable? {
    if (this == null || value == null) return null
    return ContextCompat.getDrawable(this, value)
}

fun Context?.getColorSafely(@ColorRes value: Int?): Int? {
    if (this == null || value == null) return null
    return ContextCompat.getColor(this, value)
}

fun Context?.hideKeyboard() {
    val activity = this.findActivity() ?: return
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(
        activity.window.decorView.rootView.windowToken,
        0
    )
}

fun Int.fromColorResToHexadecimalString(context: Context) = String.format("#%06x", ContextCompat.getColor(context, this) and 0xffffff)

infix fun Context.dpToPx(dpValue: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        this.resources.displayMetrics
    ).toInt()
}

fun View?.invisibleWithAccessibility() {
    this?.visibility = View.INVISIBLE
    this?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
}

fun View?.visibleWithAccessibility() {
    this?.visibility = View.VISIBLE
    this?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
}

infix fun Float.dpToPxWith(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
)

@Suppress("unchecked_cast", "deprecation")
fun <T : java.io.Serializable> Bundle.getSerializableExtra(
    key: String,
    yourClass: Class<T>
): T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    this.getSerializable(key, yourClass)
} else {
    this.getSerializable(key) as? T
}

@Suppress("deprecation", "UNUSED")
fun <T : Parcelable> Bundle.getParcelableExtra(
    key: String,
    yourClass: Class<T>
): T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    this.getParcelable(key, yourClass)
} else {
    this.getParcelable(key) as? T
}

fun RecyclerView.disableScroll() {
    when (val mManager = layoutManager) {
        is GridLayoutManager -> this.layoutManager = object : GridLayoutManager(
            context, mManager.spanCount, mManager.orientation, mManager.reverseLayout
        ) {
            override fun canScrollHorizontally() = false
            override fun canScrollVertically() = false
        }
        is LinearLayoutManager -> this.layoutManager =
            object : LinearLayoutManager(context, mManager.orientation, mManager.reverseLayout) {
                override fun canScrollHorizontally() = false
                override fun canScrollVertically() = false
            }
    }
}