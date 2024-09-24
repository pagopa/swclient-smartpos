package it.pagopa.swc_smartpos.model.presets.subscribe

import com.google.gson.annotations.SerializedName
import java.io.Serializable

//last element after splash is subscriberId
data class SubscribeTerminalResponse(@SerializedName("Location") val location: List<String>) : Serializable