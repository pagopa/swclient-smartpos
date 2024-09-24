package it.pagopa.swc.smartpos.idpay.flow.menu_test

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.all_flow.AllFlow
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.intro.IntroTest
import it.pagopa.swc.smartpos.idpay.flow.last_three.transaction_history.TransactionHistoryTest
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuTest : BaseFlowTest() {
    private val intro = this.showFragments.introTest
    private val history = this.showFragments.transactionHistory
    private val transactionDetail = this.showFragments.transactionDetail
    override fun launchActivity() {
        super.launchActivity()
        val test = AllFlow()
        if (currentActivity?.isPoynt == true) {
            allTestExceptBonus()
        } else {
            test.showFragments.loginTest.test {
                allTestExceptBonus()
            }
        }
    }

    private fun IntroTest.testWith(history: TransactionHistoryTest, onDone: Action) {
        this.fromIntroToHistory {
            history.testToWebView {
                history.test(onDone)
            }
        }
    }

    private fun allTestExceptBonus() {
        intro.testWith(history) {
            intro.fromIntroToHistory {
                history.goToTransactionDetail {
                    transactionDetail.test { }
                }
            }
        }
    }
}