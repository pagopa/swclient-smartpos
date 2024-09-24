package it.pagopa.swc_smartpos.network

import android.os.Build

internal object MyLibBuildConfig {
    fun getVersionSDKInt(): Int = Build.VERSION.SDK_INT
}