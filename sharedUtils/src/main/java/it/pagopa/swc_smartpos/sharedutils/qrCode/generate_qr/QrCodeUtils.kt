package it.pagopa.swc_smartpos.sharedutils.qrCode.generate_qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix


class QrCodeUtils {
    fun mailQrCode(mail: String, subject: String, body: String): Bitmap? {
        val encoder = QrEncoder("$mail?subject=$subject&body=$body", null, QrContents.Type.EMAIL, 250)
        return encoder.getBitmap()
    }

    fun textQrCode(text: String, centeredLogo: Bitmap? = null): Bitmap? {
        val mainBitmap = QrEncoder(text, QrContents.Type.TEXT, dimension = 250).getBitmap(isForCenteredLogo = centeredLogo != null)
        return if (centeredLogo == null)
            mainBitmap
        else {
            if (mainBitmap != null)
                mainBitmap mergeWith centeredLogo
            else
                null
        }
    }

    private infix fun Bitmap.mergeWith(bitmap2: Bitmap): Bitmap? {
        val bmOverlay = Bitmap.createBitmap(this.width, this.height, this.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(this, Matrix(), null)
        val left = this.width / 2f - bitmap2.width / 2f
        val top = this.height / 2f - bitmap2.height / 2f
        canvas.drawBitmap(bitmap2, left, top, null)
        return bmOverlay
    }
}