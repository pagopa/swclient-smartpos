package it.pagopa.swc.smartpos.idpay.base_ui_test

import it.pagopa.swc.smartpos.idpay.flow.ShowFragments

abstract class BaseTestWithShowFragment : BaseTestFragment() {
    val showFragments by lazy {
        ShowFragments()
    }
}