package it.pagopa.swc_smartpos.sharedutils.model

data class Payment(val amount: String, val status: PaymentStatus) : java.io.Serializable
enum class PaymentStatus : java.io.Serializable {
    COMPLETED,
    AUTHORIZED,
    CANCELED,
    FAILED,
    REFUNDED,
    VOIDED,
    DECLINED,
    UNKNOWN,
    FAIL_TO_LAUNCH
}