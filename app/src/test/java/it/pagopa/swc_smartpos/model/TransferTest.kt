package it.pagopa.swc_smartpos.model

import org.junit.Test

class TransferTest {
    @Test
    fun testFields() {
        val clazz = Transfer(
            paTaxCode = "paTaxCode",
            category = "category"
        )
        assert(clazz.paTaxCode == "paTaxCode")
        assert(clazz.category == "category")
    }
}