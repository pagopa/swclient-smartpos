package it.pagopa.swc_smartpos.model.presets

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreatePresetOperationResponse(@SerializedName("Location") val location: List<String>) : Serializable