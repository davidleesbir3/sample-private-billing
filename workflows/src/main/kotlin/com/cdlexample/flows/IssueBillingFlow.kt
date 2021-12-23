package com.cdlexample.flows

import co.paralleluniverse.fibers.Suspendable
import com.cdlexample.contracts.BillingContract
import com.cdlexample.states.BillingState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IssueBillingFlow (private val owner: Party) : FlowLogic<SignedTransaction>() {
    companion object {
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val operator = ourIdentity

        val billingState = BillingState(
            owner = owner,
            operator = ourIdentity,
            cumulativeUse = 0
        )

        val transactionBuilder = TransactionBuilder(notary)
            .addCommand(BillingContract.Commands.Issue(), listOf(operator.owningKey))
            .addOutputState(billingState)

        transactionBuilder.verify(serviceHub)
        val signedTx: SignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        val session = initiateFlow(owner)
        subFlow(FinalityFlow(signedTx, listOf(session)))
        return signedTx

    }
}

@InitiatedBy(IssueBillingFlow::class)
class IssueBillingResponderFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
