package it.pagopa.swc.smartpos.idpay.flow.first_four.choose_initiative

import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.databinding.ItemInitiativeBinding
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.intro.IntroTest
import it.pagopa.swc.smartpos.idpay.view.ChooseInitiative

class ChooseInitiativeTest : BaseHeaderTest() {
    private val introTest by lazy {
        IntroTest()
    }

    fun testAndGoToChooseImport(onChooseImportReached:Action) {
        testBackPress {
            introTest.goToChooseInitiative {
                testHeaderBack {
                    introTest.goToChooseInitiative {
                        goToChooseImport(action=onChooseImportReached)
                    }
                }
            }
        }
    }

    fun goToChooseImport(action: Action) {
        recyclerViewClick<ChooseInitiative.ItemInitiative, ItemInitiativeBinding>(R.id.rv_initiative, 0)
        action.invoke()
    }
}