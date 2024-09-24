package it.pagopa.swc.smartpos.idpay.flow.cie_flow

import it.pagopa.readcie.NisAuthenticated
import it.pagopa.swc.smartpos.app_shared.BaseReadCie
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.choose_import.ChooseImportTest

class CieFlowTest : BaseHeaderTest() {
    private val chooseImport by lazy {
        ChooseImportTest()
    }

    fun testAndBackHome(action: Action) {
        testBackPress {
            chooseImport.startCieFlow {
                testHeaderBack {
                    chooseImport.startCieFlow {
                        testHeaderHome(action)
                    }
                }
            }
        }
    }

    private fun transmit(action: Action) {
        val act = BaseFlowTest.currentActivity!!
        BaseFlowTest.sleepLess()
        act.viewModel.setTransmitting(true)
        BaseFlowTest.networkSleep()
        action.invoke()
    }

    private fun testError(action: Action) {
        transmit {
            BaseFlowTest.currentActivity?.viewModel?.setNisAuthenticated(
                BaseReadCie.FunInterfaceResource.error("fakeError")
            )
            BaseFlowTest.networkSleep()
            action.invoke()
        }
    }

    private fun testOk(action: Action) {
        transmit {
            BaseFlowTest.currentActivity?.viewModel?.setNisAuthenticated(
                BaseReadCie.FunInterfaceResource.success(
                    NisAuthenticated(
                        "912934523673",
                        "fakeKeyPub",
                        "fakeHash",
                        "fakeSod",
                        "fakeChallengeSigned"
                    )
                )
            )
            action.invoke()
        }
    }

    fun goToConfirmCieOp(action: Action) {
        testError {
            testOk(action)
        }
    }
}