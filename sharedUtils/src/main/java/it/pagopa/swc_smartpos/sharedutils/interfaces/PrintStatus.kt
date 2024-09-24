package it.pagopa.swc_smartpos.sharedutils.interfaces

interface PrintStatus {
    fun queued()
    fun failed()
}