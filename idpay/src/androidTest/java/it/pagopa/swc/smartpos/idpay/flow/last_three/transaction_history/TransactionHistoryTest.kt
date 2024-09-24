package it.pagopa.swc.smartpos.idpay.flow.last_three.transaction_history

import androidx.test.espresso.Espresso
import it.pagopa.swc.smartpos.app_shared.databinding.ItemMenuBinding
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.databinding.HistoryItemBinding
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.intro.IntroTest
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.app_shared.R as RShared

class TransactionHistoryTest : BaseHeaderTest() {
    private val introTest by lazy {
        IntroTest()
    }

    fun test(onBackToIntro: Action) {
        introTest.fromIntroToHistory {
            Espresso.pressBack()
            introTest.fromIntroToHistory {
                fromHistoryToIntro {
                    introTest.fromIntroToHistory {
                        testHeaderHome(onBackToIntro)
                    }
                }
            }
        }
    }
    fun goToTransactionDetail(onDone: Action){
        recyclerViewClick<Transaction, HistoryItemBinding>(R.id.history_rw, 1)
        onDone.invoke()
    }
    fun testToWebView(onDone: Action) {
        fromHistoryToWebView(onDone)
    }

    private fun fromHistoryToWebView(actionDone: Action) {
        openMenu {
            recyclerViewClick<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>(RShared.id.rv_items, 2)
            Thread.sleep(1000L)
            Espresso.pressBack()
            actionDone.invoke()
        }
    }

    private fun openMenu(onDone: Action) {
        Thread.sleep(3000L)
        click(RShared.id.iv_back)
        Thread.sleep(500L)
        onDone.invoke()
    }

    private fun fromHistoryToIntro(actionDone: Action) {
        openMenu {
            recyclerViewClick<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>(RShared.id.rv_items, 0)
            Thread.sleep(2000L)
            actionDone.invoke()
        }
    }
}