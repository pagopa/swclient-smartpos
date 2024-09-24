package it.pagopa.readcie

import it.pagopa.readcie.CieCommonMethods.transmitLogic
import it.pagopa.readcie.nfc.Utils
import org.junit.Test

class CommonMethodsTest {
    private val byteArray = byteArrayOf(0x00, 0x04, 0x05)

    @Test
    fun high_byte_and_low_byte_test() {
        val (firstExpected, secondExpected) = 0 to -1
        val (firstExpectedLowByte, secondExpectedLowByte) = 10 to -10
        assert(CieCommonMethods.highByte(10).toInt() == firstExpected)
        assert(CieCommonMethods.highByte(-10).toInt() == secondExpected)
        assert(CieCommonMethods.lowByte(10).toInt() == firstExpectedLowByte)
        assert(CieCommonMethods.lowByte(-10).toInt() == secondExpectedLowByte)
    }

    @Test
    fun unsigned_to_byte_test() {
        val expected = 10
        assert(CieCommonMethods.unsignedToBytes(10).toInt() == expected)
    }

    @Test
    fun transmit_logic_test() {
        val (firstByteArray, secondByteArray) = byteArray.transmitLogic()
        assert(Utils.bytesToString(firstByteArray) == "00")
        assert(Utils.bytesToString(secondByteArray) == "0405")
    }
}