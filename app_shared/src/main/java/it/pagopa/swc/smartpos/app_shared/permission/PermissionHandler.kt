package it.pagopa.swc.smartpos.app_shared.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity

/**Class to manage permissions*/
class PermissionHandler {
    private var permissionsNeed: MutableList<String> = mutableListOf()
    fun requestPermissions(
        context: Context?,
        arrays: Array<String>,
        permissionCallback: PermissionCallBack
    ) {
        if (context == null) return
        permissionsNeed.clear()
        pCallback = permissionCallback
        for (permission in arrays) {
            if (context needs permission)
                permissionsNeed.add(permission)
        }
        if (permissionsNeed.size > 0) {
            context.findActivity()?.let { activity ->
                ActivityCompat.requestPermissions(
                    activity,
                    permissionsNeed.toTypedArray(),
                    REQUEST_PERMISSION
                )
                permissionsNeed.forEach {
                    context firstTimeAsking it
                }
            }
        } else {
            pCallback!!.permissionGranted()
        }
    }

    private infix fun Context.needs(permission: String): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionGranted(this, permission)
    }

    fun isPermissionGranted(context: Context?, permission: String): Boolean {
        if (context == null) return false
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private infix fun Context?.firstTimeAsking(permission: String) {
        val sharedPreference = this?.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        sharedPreference?.edit()?.putBoolean(permission, false)?.apply()
    }

    private infix fun Context?.isFirstTimeAsking(permission: String): Boolean {
        val sharedPreference = this?.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreference?.getBoolean(permission, true) ?: false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermission(context: Context?, permission: String, completion: PermissionCheckCompletion) {
        // If permission is not granted
        if (!isPermissionGranted(context, permission)) {
            //If permission denied previously
            if (context.findActivity()?.shouldShowRequestPermissionRationale(permission) == true) {
                if (!(context isFirstTimeAsking permission))
                    completion(CheckPermissionResult.PermissionPreviouslyDenied)
                else
                    completion(CheckPermissionResult.PermissionAsk)
            } else {
                // Permission denied or first time requested
                if (context isFirstTimeAsking permission) {
                    completion(CheckPermissionResult.PermissionAsk)
                } else {
                    // Handle the feature without permission or ask user to manually allow permission
                    completion(CheckPermissionResult.PermissionDisabled)
                }
            }
        } else {
            completion(CheckPermissionResult.PermissionGranted)
        }
    }

    enum class CheckPermissionResult {
        PermissionAsk,
        PermissionPreviouslyDenied,
        PermissionDisabled,
        PermissionGranted
    }

    companion object {
        const val PREFS_FILE_NAME = "preference"
        const val REQUEST_PERMISSION = 1111
        var pCallback: PermissionCallBack? = null
    }
}

typealias PermissionCheckCompletion = (PermissionHandler.CheckPermissionResult) -> Unit
