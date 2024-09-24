package it.pagopa.swc_smartpos.sharedutils.nis

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes
import it.pagopa.swc_smartpos.sharedUtils.R
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import java.io.IOException

class Beep(private val activity: Activity?) {
    fun playSound(): MediaPlayer? = playAnySound(activity, R.raw.ok)
    fun playErrorSound(): MediaPlayer? = playAnySound(activity, R.raw.error)

    private fun playAnySound(context: Context?, @RawRes id: Int): MediaPlayer? {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(
                AudioAttributes.CONTENT_TYPE_MUSIC
            ).build()
        )
        mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.stop()
            mp.reset()
            mp.release()
        }
        mediaPlayer.setOnErrorListener { mp: MediaPlayer, what: Int, extra: Int ->
            WrapperLogger.i(this.javaClass.name, "Failed to beep $what, $extra")
            // possibly media player error, so release and recreate
            mp.stop()
            mp.reset()
            mp.release()
            true
        }
        try {
            val file: AssetFileDescriptor = context?.resources?.openRawResourceFd(id) ?: return null
            mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
            mediaPlayer.setVolume(1f, 1f)
            mediaPlayer.prepare()
            mediaPlayer.start()
            return mediaPlayer
        } catch (ioe: IOException) {
            WrapperLogger.e(this.javaClass.name, ioe.message.orEmpty())
            mediaPlayer.reset()
            mediaPlayer.release()
            return null
        }
    }
}