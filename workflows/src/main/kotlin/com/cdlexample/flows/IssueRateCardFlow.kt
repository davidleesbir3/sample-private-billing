package com.cdlexample.flows

import co.paralleluniverse.fibers.Suspendable
import com.cdlexample.contracts.RateCardContract
import com.cdlexample.states.RateCardState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.hibernate.Transaction
import java.lang.IllegalArgumentException

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IssueRateCard (private val rate: Long) : FlowLogic<SignedTransaction>() {
    companion object {
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call() : SignedTransaction {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        val matches: List<StateAndRef<RateCardState>> =  serviceHub.vaultService.queryBy<RateCardState>(criteria = criteria).states

        if (matches.isNotEmpty()) throw IllegalArgumentException ("A RateCard already exists. Use UpdateRateCard flow instead.")

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val operator = ourIdentity

        val rateCard = RateCardState(operator = operator, rate = rate)
        val transactionBuilder = TransactionBuilder(notary)
        transactionBuilder
            .addCommand(RateCardContract.Commands.Issue(), listOf(operator.owningKey))
            .addOutputState(rateCard)

        transactionBuilder.verify(serviceHub)
        val signedTx: SignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)
        subFlow(FinalityFlow(signedTx, emptyList()))
        return signedTx
    }
}
