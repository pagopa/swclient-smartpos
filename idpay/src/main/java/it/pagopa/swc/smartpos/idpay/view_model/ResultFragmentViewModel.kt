package it.pagopa.swc.smartpos.idpay.view_model

import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.model.response.CreateTransactionResponse
import it.pagopa.swc.smartpos.idpay.view.ResultFragment
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import it.pagopa.swc.smartpos.app_shared.R as RShared

class ResultFragmentViewModel : BaseInitiativeApiViewModel() {
    private val _operationState = MutableStateFlow<ResultFragment.OperationState?>(null)
    val operationState = _operationState.asStateFlow()
    fun setOpState(state: ResultFragment.OperationState?) {
        _operationState.value = state
    }

    fun createNewTransaction(
        frag: ResultFragment,
        bearer: String,
        errorAction: (ErrorFromDelete) -> Unit,
        isComplete: Boolean = false,
        finalAction: (Any) -> Unit
    ) {
        val saleModel = frag.mainActivity?.viewModel?.model?.value
        createTransaction(
            frag.mainActivity!!,
            bearer,
            frag.mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
            CreateTransactionRequest(
                initiativeId = saleModel?.initiative?.id.orEmpty(),
                timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ),
                goodsCost = saleModel?.amount ?: 0L
            )
        ).observe(frag.viewLifecycleOwner, BaseWrapper(frag.mainActivity, successAction = {
            frag.mainActivity?.viewModel?.setModel(
                SaleModel(
                    amount = it?.goodsCost,
                    milTransactionId = it?.milTransactionId,
                    timeStamp = it?.timestamp
                )
            )
            if (isComplete)
                finalAction.invoke(it!!)
            else
                finalAction.invoke(it?.milTransactionId.orEmpty())
        }, errorAction = {
            if (it == BaseWrapper.tokenRefreshed)
                createNewTransaction(frag, bearer, errorAction, isComplete, finalAction)
            else
                errorAction.invoke(false)
        }, showLoader = true, showDialog = false, showSecondScreenLoader = false))
    }

    fun cancelOp(frag: ResultFragment, completion: (Boolean) -> Unit) {
        frag.mainActivity?.viewModel?.setLoaderText(frag.getStringSafely(RShared.string.feedback_loading_generic))
        frag.mainActivity?.viewModel?.showLoader(true to false)
        val transactionId = frag.mainActivity?.viewModel?.model?.value?.milTransactionId.orEmpty()
        frag.accessTokenLambda { bearer ->
            deleteTransaction(
                frag.mainActivity!!,
                bearer,
                frag.mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                transactionId
            ).observe(frag.viewLifecycleOwner, BaseWrapper(frag.mainActivity, successAction = {
                completion.invoke(true)
            }, errorAction = {
                if (it == BaseWrapper.tokenRefreshed)
                    cancelOp(frag, completion)
                else
                    completion.invoke(false)
            }, showLoader = true, showDialog = false, showSecondScreenLoader = false))
        }
    }

    private fun cancelOp(
        frag: ResultFragment,
        errorAction: (ErrorFromDelete) -> Unit,
        isComplete: Boolean,
        finalAction: (Any) -> Unit
    ) {
        frag.mainActivity?.viewModel?.setLoaderText(frag.getStringSafely(RShared.string.feedback_loading_generic))
        frag.mainActivity?.viewModel?.showLoader(true to false)
        val transactionId = frag.mainActivity?.viewModel?.model?.value?.milTransactionId.orEmpty()
        frag.accessTokenLambda { bearer ->
            deleteTransaction(
                frag.mainActivity!!,
                bearer,
                frag.mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                transactionId
            ).observe(frag.viewLifecycleOwner, BaseWrapper(frag.mainActivity, successAction = {
                createNewTransaction(frag, bearer, errorAction, isComplete, finalAction)
            }, errorAction = {
                if (it == BaseWrapper.tokenRefreshed)
                    cancelOp(frag, errorAction, isComplete, finalAction)
                else
                    errorAction.invoke(true)
            }, showLoader = true, showDialog = false, showSecondScreenLoader = false))
        }
    }

    fun cancelOpAndRecreateNewComplete(
        frag: ResultFragment,
        errorAction: (ErrorFromDelete) -> Unit,
        finalAction: (CreateTransactionResponse) -> Unit
    ) {
        cancelOp(frag, errorAction, true) {
            finalAction.invoke(it as CreateTransactionResponse)
        }
    }
}

typealias ErrorFromDelete = Boolean