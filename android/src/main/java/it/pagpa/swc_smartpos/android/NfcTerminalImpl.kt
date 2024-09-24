package it.pagpa.swc_smartpos.android

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import androidx.core.os.bundleOf
import it.pagopa.readcie.ApduResponse
import it.pagopa.readcie.CieCommonMethods.transmitLogic
import it.pagopa.readcie.ReadCie
import it.pagopa.readcie.nfc.BaseNfcTerminalImpl
import it.pagopa.readcie.nfc.NfcReading
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagpa.swc_smartpos.android.utils.findActivity

class NfcTerminalImpl private constructor() : BaseNfcTerminalImpl() {
    private var adapter: NfcAdapter? = null
    private lateinit var context: Context
    private var isoDep: IsoDep? = null

    constructor(context: Context, readingInterface: NfcReading) : this() {
        this.context = context
        this.readingInterface = readingInterface
        this.adapter = NfcAdapter.getDefaultAdapter(context)
    }

    fun nfcReaderAvailable(): Boolean {
        if (!::context.isInitialized) return false
        return adapter?.isEnabled == true
    }

    override fun connect(actionDone: () -> Unit) {
        val activity = context.findActivity()
        try {
            adapter?.enableReaderMode(
                activity, {
                    if (isoDep == null)
                        isoDep = IsoDep.get(it)
                    if (isoDep?.isConnected != true)
                        isoDep?.connect()
                    if (isoDep?.isConnected == true) {
                        isoDep?.timeout = 5000
                        actionDone.invoke()
                    } else {
                        disconnect()
                        readingInterface.error("no connection to nfc tag..")
                    }
                }, NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, bundleOf(
                    NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY to 1000
                )
            )
        } catch (e: UnsupportedOperationException) {
            disconnect()
            WrapperLogger.e("NFC", e.toString())
        }
    }

    override val readCie: ReadCie
        get() = ReadCie(object : it.pagopa.readcie.OnTransmit {
            override fun error(why: String) {
                disconnect()
                readingInterface.error(why)
            }

            override fun sendCommand(apdu: ByteArray, message: String): ApduResponse {
                readingInterface.onTransmit(message)
                val resp = isoDep?.transceive(apdu)!!
                val (filteredByteArray, temp) = resp.transmitLogic()
                return ApduResponse(filteredByteArray, temp)
            }
        }, readingInterface)

    override fun disconnect() {
        val activity = context.findActivity()
        adapter?.disableReaderMode(activity)
        isoDep?.close()
        isoDep = null
    }
}