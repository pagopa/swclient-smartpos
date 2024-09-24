package it.pagopa.swc_smartpos.sharedutils.nis

fun interface OnTransmit {
    fun transmitting(isFirst: Boolean)
}