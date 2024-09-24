package it.pagopa.swc.smartpos.idpay.flow.last_three.qr_code

import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.choose_import.ChooseImportTest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import it.pagopa.swc.smartpos.idpay.view.cie_read_or_qr_code.CieReaderOrQrCodeFragment

class QrCodeFlow : BaseHeaderTest() {
    private val chooseImport by lazy {
        ChooseImportTest()
    }

    fun test(onDone: Action) {
        testHeaderBack {
            HttpServiceInterfaceMocked.cntCallForFakePoll = 0
            chooseImport.startQrCodeFlow {
                HttpServiceInterfaceMocked.cntCallForFakePoll = 2
                pressBtnAndWaitForResult()
                onDone.invoke()
            }
        }
    }

    fun testAvoid() {
        HttpServiceInterfaceMocked.cntCallForFakePoll = 2
        Thread.sleep(9200L)
        val activity = BaseFlowTest.currentActivity!!
        BaseFlowTest.setCurrentFragment()
        val fragment = BaseFlowTest.getCurrentFragment() as CieReaderOrQrCodeFragment
        activity.runOnUiThread {
            fragment.waitingDialog?.secondaryBtnAction?.invoke()
        }
        Thread.sleep(7000L)//here should be dialog are you sure
        activity.runOnUiThread {
            fragment.areYouSureDialog.secondaryBtnAction?.invoke()
        }
        BaseFlowTest.networkSleep()
    }

    fun testHome(onDone: Action) {
        testHeaderHome {
            HttpServiceInterfaceMocked.cntCallForFakePoll = 0
            Thread.sleep(500L)
            onDone.invoke()
        }
    }

    fun testWithoutBack(onDone: Action) {
        HttpServiceInterfaceMocked.cntCallForFakePoll = 2
        pressBtnAndWaitForResult()
        onDone.invoke()
    }

    private fun pressBtnAndWaitForResult() {
        click(R.id.btn_trouble_with_qr)
        Thread.sleep(20000L)
    }
}