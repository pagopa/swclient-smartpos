package it.pagopa.readcie

interface OnTransmit {
    fun sendCommand(apdu: ByteArray, message: String): ApduResponse
    fun error(why: String)
}