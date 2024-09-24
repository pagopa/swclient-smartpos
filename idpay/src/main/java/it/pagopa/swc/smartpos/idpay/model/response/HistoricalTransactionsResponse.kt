package it.pagopa.swc.smartpos.idpay.model.response


import com.google.gson.annotations.SerializedName

data class HistoricalTransactionsResponse(
    @SerializedName("transactions")
    val transactions: List<Transaction>?
) : java.io.Serializable