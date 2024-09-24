package it.pagpa.swc_smartpos.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.interfaces.PrintStatus
import java.io.File
import java.io.FileOutputStream


class AndroidWrapper {
    fun printDrawable(context: Context, act: Activity, drawable: Drawable, height: Int, status: PrintStatus) {
        try {
            val icon: Bitmap = drawable.toBitmap(400, height)
            // Create a PdfDocument with a page of the same size as the image
            val document = PdfDocument()
            val pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(icon.width, icon.height, 1).create()
            val page: PdfDocument.Page = document.startPage(pageInfo)

            // Draw the bitmap onto the page
            val canvas: Canvas = page.canvas
            canvas.drawBitmap(icon, 0f, 0f, null)
            document.finishPage(page)

            // Write the PDF file to a file
            val directoryPath: File = act.cacheDir
            val file = File(directoryPath, "example.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()
            val uri = FileProvider.getUriForFile(
                act,
                "${context.packageName}.provider",
                file
            )
            val share = Intent(Intent.ACTION_SEND)
            share.type = "application/pdf"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            act.startActivity(Intent.createChooser(share, "Share Image"))
            status.queued()
        } catch (e: Exception) {
            WrapperLogger.e(e.javaClass.name, e.message.orEmpty())
            status.failed()
        }
    }
}