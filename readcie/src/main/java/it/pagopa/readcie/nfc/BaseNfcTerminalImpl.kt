package it.pagopa.readcie.nfc

import it.pagopa.readcie.ReadCie

abstract class BaseNfcTerminalImpl {
    lateinit var readingInterface: NfcReading
    abstract fun connect(actionDone: () -> Unit)
    abstract val readCie: ReadCie
    abstract fun disconnect()
    fun transmit(challenge: String) {
        connect {
            readingInterface.onTransmit("connected")
            readCie.read(challenge) {
                disconnect()
            }
        }
    }
}