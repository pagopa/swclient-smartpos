package it.pagopa.swc_smartpos.model

import org.junit.Test

class QrCodeVerifyResponseTest {
    @Test
    fun leftToTest() {
        val clazz = QrCodeVerifyResponse(
            amount = 100,
            company = "company",
            description = "description",
            paTaxCode = "paTaxCode",
            noticeNumber = "noticeNumber",
            note = "note",
            office = "office"
        )
        assert(clazz.amount == 100)
        assert(clazz.company == "company")
        assert(clazz.description == "description")
        assert(clazz.paTaxCode == "paTaxCode")
        assert(clazz.noticeNumber == "noticeNumber")
        assert(clazz.note == "note")
        assert(clazz.office == "office")
    }
}