package it.pagopa.swc.smartpos.idpay.flow.all_flow

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.receipt.ReceiptFlow
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieTest : BaseFlowTest() {
    private val intro = this.showFragments.introTest
    private val chooseInitiative = this.showFragments.chooseInitiativeTest
    private val chooseImport = this.showFragments.chooseImportTest
    private val cieFlow = this.showFragments.cieFlow
    private val confirmCieOp = this.showFragments.confirmCieOp
    private val ciePin = this.showFragments.ciePin
    private val result = this.showFragments.resultFlow
    private val receipt = this.showFragments.receiptFlow
    private val outro = this.showFragments.outroFlow
    private val residual = this.showFragments.payResidualFlow
    private fun directToCie(action: Action) {
        HttpServiceInterfaceMocked.cntCallForFakePoll = 0
        intro.goToChooseInitiative {
            chooseInitiative.goToChooseImport {
                chooseImport.startCieFlow(action)
            }
        }
    }

    private fun directToCiePin(action: Action) {
        directToCie {
            cieFlow.goToConfirmCieOp {
                HttpServiceInterfaceMocked.cntCallForFakePoll = 2
                Thread.sleep(10000L)
                confirmCieOp.gotToCiePin(action)
            }
        }
    }

    private fun testCieTooMuchPin() {
        directToCiePin {
            ciePin.goToTooMuch {
                networkSleep()
            }
        }
    }

    private fun testCie() {
        directToCie {
            cieFlow.testAndBackHome {
                directToCie {
                    cieFlow.goToConfirmCieOp {
                        HttpServiceInterfaceMocked.cntCallForFakePoll = 2
                        Thread.sleep(10000L)
                        confirmCieOp.denyOpAndBackToCieReader {
                            cieFlow.goToConfirmCieOp {
                                HttpServiceInterfaceMocked.cntCallForFakePoll = 2
                                Thread.sleep(10000L)
                                confirmCieOp.gotToCiePin {
                                    ciePin.testAndBackHomeWithDialog {
                                        directToCiePin {
                                            ciePin.goToResult {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun launchActivity() {
        super.launchActivity()
        val allFlowTest = AllFlow()
        if (currentActivity?.isPoynt == true) {
            testCie()
        } else {
            allFlowTest.showFragments.loginTest.goToIntro {
                networkSleep()
                testCie()
            }
        }
    }

    private fun ReceiptFlow.clickShareOrGoToOutro(action: Action) {
        if (BuildConfig.FLAVOR.contains("androidNative", true)) {
            this.clickShare(action)
        } else
            this.goToOutro(action)
    }

    private fun testDeleteResidualPayment() {
        directToCiePin {
            ciePin.goToResult {
                result.goToNeedReceipt {
                    receipt.clickShareOrGoToOutro {
                        sleepLess()
                        outro.goToResidual {
                            residual.testCancelOp {
                                networkSleep()
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun deleteResidualPaymentTest() {
        super.launchActivity()
        val allFlowTest = AllFlow()
        if (currentActivity?.isPoynt == true) {
            testDeleteResidualPayment()
        } else {
            allFlowTest.showFragments.loginTest.goToIntro {
                networkSleep()
                testDeleteResidualPayment()
            }
        }
    }

    @Test
    fun tooMuchWrongPin(){
        super.launchActivity()
        val allFlowTest = AllFlow()
        if (currentActivity?.isPoynt == true) {
            testCieTooMuchPin()
        } else {
            allFlowTest.showFragments.loginTest.goToIntro {
                networkSleep()
                testCieTooMuchPin()
            }
        }
    }
}