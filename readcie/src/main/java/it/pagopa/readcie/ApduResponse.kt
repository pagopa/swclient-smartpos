package it.pagopa.readcie

import it.pagopa.readcie.nfc.Utils


class ApduResponse private constructor() {
    lateinit var response: ByteArray
    lateinit var swByte: ByteArray

    constructor(fullResponse: ByteArray) : this() {
        response = fullResponse.copyOfRange(0, fullResponse.size - 2)
        swByte = fullResponse.copyOfRange(fullResponse.size - 2, fullResponse.size)
    }

    constructor(res: ByteArray, sw: ByteArray) : this() {
        response = res
        swByte = sw
    }

    val swHex: String get() = Utils.bytesToString(swByte)
    val swInt: Int get() = Utils.toUint(swByte)

    override fun toString(): String {
        return "ApduResponse: swHex:$swHex, swInt:$swInt"
    }
}