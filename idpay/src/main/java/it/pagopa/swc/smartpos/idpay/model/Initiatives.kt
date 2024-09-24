package it.pagopa.swc.smartpos.idpay.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Initiatives(
    @SerializedName("initiatives") val initiatives: List<InitiativeModel>?
) : Serializable {
    data class InitiativeModel(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("organization") val organization: String
    ) : Serializable
}