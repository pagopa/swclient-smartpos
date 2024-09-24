package it.pagopa.swc_smartpos.sharedutils.model

data class Business(
    val id: String, val terminalId: String,
    val merchantId: String,
    val acquirerIdList: List<String>,
    val paTaxCode: String? = null
) : java.io.Serializable