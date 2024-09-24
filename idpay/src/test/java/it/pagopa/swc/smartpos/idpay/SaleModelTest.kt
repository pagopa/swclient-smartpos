package it.pagopa.swc.smartpos.idpay

import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class SaleModelTest {
    @Test
    fun testPercentage() {
        val model = SaleModel(null, null, null, 99.21f)
        assert(model.humanPercentage() == "99,21")
        val modelTwo = SaleModel(null, null, null, 99.2134f)
        assert(modelTwo.humanPercentage() == "99,21")
        val modelThree = SaleModel(null, null, null, 99f)
        assert(modelThree.humanPercentage() == "99")
    }

    @Test
    fun isCanceledOpTest() {
        val model = SaleModel(null, null, null, 99.21f)
        assert(!model.isAllNull())
        val modelTwo = SaleModel(Initiatives.InitiativeModel("", "", ""), null, null, null)
        assert(!modelTwo.isAllNull())
        val modelThree = SaleModel(null, 2500, null, null)
        assert(!modelThree.isAllNull())
        val modelFour = SaleModel(null, null, 2500, null)
        assert(!modelFour.isAllNull())
        val modelFive = SaleModel(null, null, null, null)
        assert(modelFive.isAllNull())
        val lastModel = SaleModel(Initiatives.InitiativeModel("", "", ""), 2500, 2500, 99.21f)
        assert(!lastModel.isAllNull())
        lastModel.voidOp()
        assert(lastModel.isAllNull())
    }

    @Test
    fun setParamTest() {
        val initiatives = Initiatives(listOf(Initiatives.InitiativeModel("", "", "")))
        val model = MutableStateFlow(SaleModel(initiatives.initiatives?.get(0), 2500, 2500, 99.21f))
        var initiative = model.value.initiative!!
        assert(initiative.id == "" && initiative.name == "" && initiative.organization == "")
        assert(model.value.amount == 2500L)
        assert(model.value.availableSale == 2500L)
        assert(model.value.percentageSale == 99.21f)
        model.value.set(model, SaleModel(amount = 3500L))
        model.value.set(model, SaleModel(initiative = Initiatives.InitiativeModel("fakeId", "fakeName", "fakeOrganization")))
        model.value.set(model, SaleModel(availableSale = 3500L))
        model.value.set(model, SaleModel(percentageSale = 88f))
        initiative = model.value.initiative!!
        assert(initiative.id == "fakeId" && initiative.name == "fakeName" && initiative.organization == "fakeOrganization")
        assert(model.value.amount == 3500L)
        assert(model.value.availableSale == 3500L)
        assert(model.value.percentageSale == 88f)
    }
}