package it.pagopa.swc_smartpos.sharedutils.vibrate

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class VibrateManager {
    fun vibrate(context: Context?, howMuch: Long) {
        val vibrator: Vibrator?
        val vibratorManager: VibratorManager?
        if (Build.VERSION.SDK_INT >= 31) {
            vibratorManager = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibrator = vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createOneShot(howMuch, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(howMuch)
        }
    }
}