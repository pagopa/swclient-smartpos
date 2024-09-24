package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName

data class HistoricalTransactionResponse(
    @SerializedName("transactions")
    val transactions: List<Transaction>
) : java.io.Serializable