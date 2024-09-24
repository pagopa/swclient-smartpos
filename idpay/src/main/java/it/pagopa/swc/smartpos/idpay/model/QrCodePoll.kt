package it.pagopa.swc.smartpos.idpay.model

import java.io.Serializable

data class QrCodePoll(val qrCode: String?, val qrCodeFallback: String?) : Serializable
