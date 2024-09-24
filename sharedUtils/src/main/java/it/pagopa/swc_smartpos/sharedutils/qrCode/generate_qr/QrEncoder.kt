package it.pagopa.swc_smartpos.sharedutils.qrCode.generate_qr

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap
import java.util.Locale

@Suppress("UNUSED")
class QrEncoder {
    private var white = -0x1
    private var black = -0x1000000
    private var dimension = Int.MIN_VALUE
    private var contents: String? = null
    private var displayContents: String? = null
    private var title: String? = null
    private val format = BarcodeFormat.QR_CODE
    private var encoded = false

    fun setColorWhite(color: Int) {
        white = color
    }

    fun setColorBlack(color: Int) {
        black = color
    }

    fun getColorWhite(): Int {
        return white
    }

    fun getColorBlack(): Int {
        return black
    }

    constructor(data: String?, type: String) {
        encoded = encodeContents(data, null, type)
    }

    constructor(data: String?, type: String, dimension: Int) {
        this.dimension = dimension
        encoded = encodeContents(data, null, type)
    }

    constructor(data: String?, bundle: Bundle?, type: String, dimension: Int) {
        this.dimension = dimension
        encoded = encodeContents(data, bundle, type)
    }

    fun getTitle(): String? {
        return title
    }

    private fun encodeContents(data: String?, bundle: Bundle?, type: String): Boolean {
        // Default to QR_CODE if no format given.
        encodeQRCodeContents(data, bundle, type)
        return contents != null && contents!!.isNotEmpty()
    }

    private fun encodeQRCodeContents(data: String?, bundle: Bundle?, type: String) {
        var mData = data
        when (type) {
            QrContents.Type.TEXT -> if (!mData.isNullOrEmpty()) {
                contents = mData
                displayContents = mData
                title = "Text"
            }

            QrContents.Type.EMAIL -> {
                mData = trim(mData)
                if (mData != null) {
                    contents = "mailto:$mData"
                    displayContents = mData
                    title = "E-Mail"
                }
            }

            QrContents.Type.PHONE -> {
                mData = trim(mData)
                if (mData != null) {
                    contents = "tel:$mData"
                    displayContents = PhoneNumberUtils.formatNumber(mData, Locale.getDefault().country)
                    title = "Phone"
                }
            }

            QrContents.Type.SMS -> {
                mData = trim(mData)
                if (mData != null) {
                    contents = "sms:$mData"
                    displayContents = PhoneNumberUtils.formatNumber(mData, Locale.getDefault().country)
                    title = "SMS"
                }
            }

            QrContents.Type.CONTACT -> if (bundle != null) {
                val newContents = StringBuilder(100)
                val newDisplayContents = StringBuilder(100)
                newContents.append("BEGIN:VCARD\n")
                val name = trim(bundle.getString(ContactsContract.Intents.Insert.NAME))
                if (name != null) {
                    newContents.append("N:").append(escapeVCard(name)).append(';')
                    newDisplayContents.append(name)
                    newContents.append("\n")
                }
                val address = trim(bundle.getString(ContactsContract.Intents.Insert.POSTAL))
                if (address != null) {
                    //the append ; is removed because it is unnecessary because we are breaking into new row
                    newContents.append("ADR:").append(escapeVCard(address)) //.append(';')
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(address)
                }
                val uniquePhones: MutableCollection<String> = HashSet(QrContents.PHONE_KEYS.size)
                run {
                    var x = 0
                    while (x < QrContents.PHONE_KEYS.size) {
                        val phone = trim(bundle.getString(QrContents.PHONE_KEYS[x]))
                        if (phone != null) {
                            uniquePhones.add(phone)
                        }
                        x++
                    }
                }
                for (phone in uniquePhones) {
                    newContents.append("TEL:").append(escapeVCard(phone)) //.append(';')
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(PhoneNumberUtils.formatNumber(phone, Locale.getDefault().country))
                }
                val uniqueEmails: MutableCollection<String> = HashSet(QrContents.EMAIL_KEYS.size)
                var x = 0
                while (x < QrContents.EMAIL_KEYS.size) {
                    val email = trim(bundle.getString(QrContents.EMAIL_KEYS[x]))
                    if (email != null) {
                        uniqueEmails.add(email)
                    }
                    x++
                }
                for (email in uniqueEmails) {
                    newContents.append("EMAIL:").append(escapeVCard(email)) //.append(';')
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(email)
                }
                val organization = trim(bundle.getString(ContactsContract.Intents.Insert.COMPANY))
                if (organization != null) {
                    newContents.append("ORG:").append(organization) //.append(';')
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(organization)
                }
                val url = trim(bundle.getString(ContactsContract.Intents.Insert.DATA))
                if (url != null) {
                    // in this field only the website name and the domain are necessary (example : somewebsite.com)
                    newContents.append("URL:").append(escapeVCard(url)) //.append(';');
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(url)
                }
                val note = trim(bundle.getString(ContactsContract.Intents.Insert.NOTES))
                if (note != null) {
                    newContents.append("NOTE:").append(escapeVCard(note)) //.append(';')
                    newContents.append("\n")
                    newDisplayContents.append('\n').append(note)
                }

                // Make sure we've encoded at least one field.
                if (newDisplayContents.isNotEmpty()) {
                    //this end vcard needs to be at the end in order for the default phone reader to recognize it as a contact
                    newContents.append("END:VCARD")
                    newContents.append(';')
                    contents = newContents.toString()
                    displayContents = newDisplayContents.toString()
                    title = "Contact"
                } else {
                    contents = null
                    displayContents = null
                }
            }

            QrContents.Type.LOCATION -> if (bundle != null) {
                // These must use Bundle.getFloat(), not getDouble(), it's part of the API.
                val latitude = bundle.getFloat("LAT", Float.MAX_VALUE)
                val longitude = bundle.getFloat("LONG", Float.MAX_VALUE)
                if (latitude != Float.MAX_VALUE && longitude != Float.MAX_VALUE) {
                    contents = "geo:$latitude,$longitude"
                    displayContents = "$latitude,$longitude"
                    title = "Location"
                }
            }
        }
    }

    fun getBitmap(margin: Int = 0, isForCenteredLogo: Boolean = false): Bitmap? {
        return if (!encoded) null else try {
            val hints: MutableMap<EncodeHintType?, Any?>?
            hints = EnumMap(EncodeHintType::class.java)
            val encoding = guessAppropriateEncoding(contents)
            if (encoding != null) {
                if (isForCenteredLogo)
                    hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
                hints[EncodeHintType.CHARACTER_SET] = encoding
            }
            // Setting the margin width
            hints[EncodeHintType.MARGIN] = margin
            val writer = MultiFormatWriter()
            val result = writer.encode(contents, format, dimension, dimension, hints)
            val width = result.width
            val height = result.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (result[x, y]) black else white
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (ex: Exception) {
            null
        }
    }

    private fun guessAppropriateEncoding(contents: CharSequence?): String? {
        // Very crude at the moment
        contents?.forEach {
            if (it.code > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }

    private fun trim(s: String?): String? {
        if (s == null) {
            return null
        }
        val result = s.trim { it <= ' ' }
        return result.ifEmpty { null }
    }

    private fun escapeVCard(input: String?): String? {
        if (input == null || input.indexOf(':') < 0 && input.indexOf(';') < 0) {
            return input
        }
        val length = input.length
        val result = StringBuilder(length)
        for (i in 0 until length) {
            val c = input[i]
            if (c == ':' || c == ';') {
                result.append('\\')
            }
            result.append(c)
        }
        return result.toString()
    }
}