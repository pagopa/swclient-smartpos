import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import it.pagopa.readcie.nfc.NfcReading
import it.pagopa.swc.smartpos.app_shared.BaseReadCie
import it.pagopa.swc.smartpos.app_shared.BuildConfig
import it.pagpa.swc_smartpos.android.NfcTerminalImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadCIE(private val ownerActivity: Activity, challenge: String? = null) : BaseReadCie(challenge) {
    private var implementation: NfcTerminalImpl? = null
    override suspend fun workNfc(challenge: String, readingInterface: NfcReading) {
        withContext(Dispatchers.Default) {
            implementation = NfcTerminalImpl(ownerActivity, readingInterface)
            if (implementation?.nfcReaderAvailable() != true && !BuildConfig.FLAVOR_environment.equals("mock", true))
                readingInterface.error("NFC not available")
            else {
                try {
                    implementation!!.transmit(challenge)
                } catch (e: Exception) {
                    readingInterface.error(e.message.orEmpty())
                }
            }
        }
    }

    override fun disconnect() {
        implementation?.disconnect()
    }

    companion object {
        fun isNfcAvailable(context: Context?) = NfcAdapter.getDefaultAdapter(context)?.isEnabled == true || BuildConfig.FLAVOR_environment.equals("mock", true)
    }
}