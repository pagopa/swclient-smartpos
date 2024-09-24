package it.pagopa.readcie

import it.pagopa.readcie.CieCommonMethods.transmitLogic
import it.pagopa.readcie.nfc.Utils
import org.junit.Test

class UtilsTest {
    private val byteArray = byteArrayOf(0x00, 0x04, 0x05)

    @Test
    fun transmit_logic_test() {
        val (firstByteArray, secondByteArray) = byteArray.transmitLogic()
        assert(Utils.bytesToString(firstByteArray) == "00")
        assert(Utils.bytesToString(secondByteArray) == "0405")
    }

    @Test
    fun append_byte_array_test() {
        val expected = byteArrayOf(0x00, 0x04, 0x05, 0x02, 0x05, 0x0a)
        val backByF = Utils.appendByteArray(byteArray, byteArrayOf(0x02, 0x05, 0x0a))
        assert(expected.size == backByF.size)
        backByF.forEachIndexed { i, byte ->
            assert(byte == expected[i])
        }
    }

    @Test
    fun get_sub_test() {
        val expected = "000000"
        assert(Utils.bytesToString(Utils.getSub(byteArray, 0, 3)) == expected)
    }

    @Test
    fun append_byte_test() {
        val expected = "00040506"
        assert(Utils.bytesToString(Utils.appendByte(byteArray, 0x06)) == expected)
    }

    @Test
    fun to_UInt_test() {
        val expected = 1029
        assert(Utils.toUint(byteArray) == expected)
    }

    @Test
    fun byte_compare_test() {
        assert(Utils.byteCompare(0x00, 0x00) == 0)
        assert(Utils.byteCompare(0x00, 0x01) == -1)
    }

    @Test
    fun getLeftTest() {
        val expectedOne = "000405"
        assert(Utils.bytesToString(Utils.getLeft(byteArray, 10)) == expectedOne)
        val expectedTwo = "0004"
        assert(Utils.bytesToString(Utils.getLeft(byteArray, 2)) == expectedTwo)
        val expectedThree = "00"
        assert(Utils.bytesToString(Utils.getLeft(byteArray, 1)) == expectedThree)
        assert(Utils.bytesToString(Utils.getLeft(byteArray, 3)) == expectedOne)
        assert(Utils.bytesToString(Utils.getLeft(byteArray, 0)) == "")
    }

    @Test
    fun asn1_tag_test() {
        var byteArrayAsn1 = byteArray.toList()//we need a byte array with length more than 128 to cover more cases
        for (i in 0 until 50) {//finally we will got a 150 size array
            byteArrayAsn1 = byteArrayAsn1 + byteArray.toList()
        }
        val (expectedOne, expectedTwo) = "0A03000405" to "0203000405"
        assert(Utils.bytesToString(Utils.asn1Tag(byteArray, 10)) == expectedOne)
        assert(Utils.bytesToString(Utils.asn1Tag(byteArray, 2)) == expectedTwo)
        val (expectedOneMoreThan128, expectedTwoMoreThan128) = "0A819900040500040500040500040500040500040" +
                "50004050004050004050004050004050004050004050004050004050004050004050004050" +
                "00405000405000405000405000405000405000405000405000405000405000405000405000" +
                "4050004050004050004050004050004050004050004050004050004050004050004050004050004" +
                "05000405000405000405000405000405000405000405" to "028199000405000405000405000405000405000405000405000" +
                "4050004050004050004050004050004050004050004050004050004050004050004050004050004050004050004050" +
                "0040500040500040500040500040500040500040500040500040500040500040500040500" +
                "0405000405000405000405000405000405000405000405000405000405000405000405000405000405000405000405"
        assert(Utils.bytesToString(Utils.asn1Tag(byteArrayAsn1.toByteArray(), 10)) == expectedOneMoreThan128)
        assert(Utils.bytesToString(Utils.asn1Tag(byteArrayAsn1.toByteArray(), 2)) == expectedTwoMoreThan128)
    }

    @Test
    fun tag_to_byte() {
        val expected = "01F4"
        val back = Utils.tagToByte(500)
        assert(Utils.bytesToString(back) == expected)
        val expected2 = "0186A0"
        val back2 = Utils.tagToByte(100000)
        assert(Utils.bytesToString(back2) == expected2)
    }

    @Test
    fun len_to_byte(){
        val expected = "8201F4"
        val back = Utils.lenToBytes(500)
        assert(Utils.bytesToString(back) == expected)
        val expected2 = "830186A0"
        val back2 = Utils.lenToBytes(100000)
        assert(Utils.bytesToString(back2) == expected2)
        val expected3 = "81C8"
        val back3 = Utils.lenToBytes(200)
        assert(Utils.bytesToString(back3) == expected3)
        val expected4 = ""
        val back4 = Utils.lenToBytes(167077216)
        assert(Utils.bytesToString(back4) == expected4)
    }
}