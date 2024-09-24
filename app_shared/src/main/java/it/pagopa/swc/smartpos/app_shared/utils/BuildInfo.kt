package it.pagopa.swc.smartpos.app_shared.utils

import android.annotation.SuppressLint
import android.os.Build
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlin.properties.Delegates

@Suppress("DEPRECATION")
class TerminalInfoWrapper private constructor() {
    private lateinit var buildInfo: BuildInfo
    private lateinit var versionInfo: BuildInfo.VersionInfo
    fun json(): String = Gson().toJson(this)

    companion object {
        fun build() = TerminalInfoWrapper().apply {
            buildInfo = BuildInfo.build()
            versionInfo = BuildInfo.VersionInfo.build()
        }
    }

    private class BuildInfo private constructor() {
        private lateinit var device: String
        private lateinit var display: String
        private lateinit var model: String
        private lateinit var manufacturer: String
        private lateinit var board: String
        private lateinit var bootloader: String
        private lateinit var brand: String
        private lateinit var fingerprint: String
        private lateinit var hardware: String
        private lateinit var host: String
        private lateinit var id: String
        private lateinit var odmSku: String
        private lateinit var sku: String
        private lateinit var socManufacturer: String
        private lateinit var socModel: String
        private lateinit var supported32BitsAbis: List<String>
        private lateinit var supported64BitsAbis: List<String>
        private lateinit var supportedAbis: List<String>
        private lateinit var serial: String
        private lateinit var tags: String
        private lateinit var product: String
        @delegate:SerializedName("time")
        @get:SerializedName("time")
        private var time by Delegates.notNull<Long>()
        private lateinit var user: String
        private lateinit var type: String

        class VersionInfo private constructor() {
            @delegate:SerializedName("sdkInt")
            @get:SerializedName("sdkInt")
            private var sdkInt by Delegates.notNull<Int>()
            private lateinit var sdk: String
            private lateinit var baseOs: String
            private lateinit var codeName: String
            private lateinit var incremental: String
            @delegate:SerializedName("mediaPerformanceClass")
            @get:SerializedName("mediaPerformanceClass")
            private var mediaPerformanceClass by Delegates.notNull<Int>()
            @delegate:SerializedName("previewSdkInt")
            @get:SerializedName("previewSdkInt")
            private var previewSdkInt by Delegates.notNull<Int>()
            private lateinit var release: String
            private lateinit var releaseOrCodeName: String
            private lateinit var releaseOrPreviewDisplay: String
            private lateinit var securityPatch: String

            companion object {
                fun build() = VersionInfo().apply {
                    sdkInt = Build.VERSION.SDK_INT
                    baseOs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Build.VERSION.BASE_OS
                    } else ""
                    sdk = Build.VERSION.SDK
                    codeName = Build.VERSION.CODENAME
                    incremental = Build.VERSION.INCREMENTAL
                    mediaPerformanceClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Build.VERSION.MEDIA_PERFORMANCE_CLASS
                    } else 0
                    previewSdkInt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Build.VERSION.PREVIEW_SDK_INT
                    } else 0
                    release = Build.VERSION.RELEASE
                    releaseOrCodeName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Build.VERSION.RELEASE_OR_CODENAME
                    } else ""
                    releaseOrPreviewDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY
                    } else ""
                    securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Build.VERSION.SECURITY_PATCH
                    } else ""
                }
            }
        }

        companion object {
            @SuppressLint("HardwareIds")
            fun build() = BuildInfo().apply {
                device = Build.DEVICE
                display = Build.DISPLAY
                model = Build.MODEL
                manufacturer = Build.MANUFACTURER
                board = Build.BOARD
                bootloader = Build.BOOTLOADER
                brand = Build.BRAND
                fingerprint = Build.FINGERPRINT
                hardware = Build.HARDWARE
                host = Build.HOST
                id = Build.ID
                odmSku = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Build.ODM_SKU
                } else ""
                sku = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Build.SKU
                } else ""
                socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Build.SOC_MANUFACTURER
                } else ""
                socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Build.SOC_MODEL
                } else ""
                supported32BitsAbis = Build.SUPPORTED_32_BIT_ABIS.toList()
                supported64BitsAbis = Build.SUPPORTED_64_BIT_ABIS.toList()
                supportedAbis = Build.SUPPORTED_ABIS.toList()
                serial = Build.SERIAL
                Build.VERSION.SDK_INT
                tags = Build.TAGS
                product = Build.PRODUCT
                time = Build.TIME
                user = Build.USER
                type = Build.TYPE
            }
        }
    }
}