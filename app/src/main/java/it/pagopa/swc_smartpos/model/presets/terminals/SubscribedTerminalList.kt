package it.pagopa.swc_smartpos.model.presets.terminals


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubscribedTerminalList(
    @SerializedName("subscribers")
    val subscribers: List<Subscriber>
) : Serializable {
    data class Subscriber(
        @SerializedName("acquirerId")
        val acquirerId: String,
        @SerializedName("channel")
        val channel: String,
        @SerializedName("label")
        val label: String,
        @SerializedName("lastUsageTimestamp")
        val lastUsageTimestamp: String,
        @SerializedName("merchantId")
        val merchantId: String,
        @SerializedName("paTaxCode")
        val paTaxCode: String,
        @SerializedName("subscriberId")
        val subscriberId: String,
        @SerializedName("subscriptionTimestamp")
        val subscriptionTimestamp: String,
        @SerializedName("terminalId")
        val terminalId: String
    ) : Serializable
}