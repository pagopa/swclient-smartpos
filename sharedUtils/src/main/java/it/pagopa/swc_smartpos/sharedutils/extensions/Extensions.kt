package it.pagopa.swc_smartpos.sharedutils.extensions

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun ViewModel.launchTimer(sec: Int, onEnd: () -> Unit) {
    viewModelScope.launch {
        (0..60)
            .asSequence()
            .asFlow()
            .onEach { delay(1_000) }.collectLatest {
                if (it == sec)
                    onEnd.invoke()
            }
    }
}

fun CharSequence?.orEmptyCharSeq() = if (this.isNullOrEmpty()) "" else this
fun Number.toAmountFormatted() = BigDecimal(this.toString()).movePointLeft(2).toString().replace(".", ",") + " €"

fun String.dateStringToTimestamp(): Long? {
    val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return formatter.parse(this)?.time
}

fun Long?.transactionTimeString() = this?.let { Date(it) }?.let {
    SimpleDateFormat("dd MMM yyyy',' HH:mm", Locale.getDefault()).format(
        it
    ).uppercase()
}

fun Bitmap.toBlackAndWhite(): Bitmap {
    val width = this.width
    val height = this.height
    val bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val factor = 255f
    val redBri = 0.2126f
    val greenBri = 0.2126f
    val blueBri = 0.0722f
    val length = width * height
    val inPixels = IntArray(length)
    val outPixels = IntArray(length)
    this.getPixels(inPixels, 0, width, 0, 0, width, height)
    for ((point, pix) in inPixels.withIndex()) {
        val r = pix shr 16 and 0xFF
        val g = pix shr 8 and 0xFF
        val b = pix and 0xFF
        val lum = redBri * r / factor + greenBri * g / factor + blueBri * b / factor
        if (lum > 0.4) {
            outPixels[point] = -0x1
        } else {
            outPixels[point] = -0x1000000
        }
    }
    bmOut.setPixels(outPixels, 0, width, 0, 0, width, height)
    return bmOut
}

fun Token.toExpirationTime(): Long {
    return SignedJWT.parse(this).jwtClaimsSet.expirationTime.time
}
typealias Token = String