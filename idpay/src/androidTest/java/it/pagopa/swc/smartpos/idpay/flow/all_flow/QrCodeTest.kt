package it.pagopa.swc.smartpos.idpay.flow.all_flow

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.receipt.ReceiptFlow
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeTest : BaseFlowTest() {
    private val intro = this.showFragments.introTest
    private val chooseInitiative = this.showFragments.chooseInitiativeTest
    private val chooseImport = this.showFragments.chooseImportTest
    private val qrCode = this.showFragments.qrCodeFlow
    private val result = this.showFragments.resultFlow
    private val receipt = this.showFragments.receiptFlow
    private val outro = this.showFragments.outroFlow
    private val residual = this.showFragments.payResidualFlow

    private fun directToQrCode(action: Action) {
        intro.goToChooseInitiative {
            chooseInitiative.goToChooseImport {
                chooseImport.startQrCodeFlow(action)
            }
        }
    }

    private fun ReceiptFlow.clickShareOrGoToOutro(action: Action) {
        if (BuildConfig.FLAVOR.contains("androidNative", true)) {
            this.clickShare(action)
        } else
            this.goToOutro(action)
    }

    private fun testQrCode() {
        intro.goToChooseInitiative {
            chooseInitiative.testAndGoToChooseImport {
                chooseImport.testAndGoToQrCode {
                    qrCode.testHome {
                        directToQrCode {
                            qrCode.test {
                                result.goToNeedReceipt {
                                    receipt.clickShareOrGoToOutro {
                                        sleepLess()
                                        outro.goToResidual {
                                            residual.testBackHome {
                                                directToQrCode {
                                                    qrCode.testWithoutBack {
                                                        result.goToNeedReceipt {
                                                            receipt.goToOutro {
                                                                outro.goToResidualWithoutDialog {
                                                                    residual.testPayWithCash()
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
                        }
                    }
                }
            }
        }
    }

    private fun testAvoid() {
        directToQrCode {
            qrCode.testAvoid()
        }
    }

    override fun launchActivity() {
        super.launchActivity()
        val allFlowTest = AllFlow()
        if (currentActivity?.isPoynt == true) {
            testQrCode()
        } else {
            allFlowTest.showFragments.loginTest.goToIntro {
                testQrCode()
            }
        }
    }

    @Test
    fun avoidDialogTest() {
        super.launchActivity()
        val allFlowTest = AllFlow()
        if (currentActivity?.isPoynt == true) {
            testAvoid()
        } else {
            allFlowTest.showFragments.loginTest.goToIntro {
                testAvoid()
            }
        }
    }
}