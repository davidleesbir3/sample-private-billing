package com.cdlexample.contracts

import com.cdlexample.states.BillingState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class BillingContract: Contract {
    companion object {
        const val ID = "com.cdlexample.contracts.BillingContract"
    }

    interface Commands : CommandData {
        class Issue: Commands
        class Bill: Commands
        class Redeem: Commands
    }


    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<BillingContract.Commands>()
        val inputs = tx.inputs
        val outputs = tx.outputs
        when (command.value) {
            is Commands.Issue -> requireThat {
                "There should be no input for Issue command." using (inputs.isEmpty())
                "There should be exactly one BillingState in the output for Issue command" using ( outputs.size == 1 && tx.outputsOfType<BillingState>().isNotEmpty())
                "Operator must be signer for Issue command." using (command.signers.toSet().contains(tx.outputsOfType<BillingState>().single().operator.owningKey))
            }
            is Commands.Bill -> requireThat {
                "There should be exactly one BillingState in the input for Bill command." using (inputs.size == 1 && tx.inputsOfType<BillingState>().isNotEmpty())
                "There should be exactly one BillingState in the output for Bill command." using ( outputs.size == 1 && tx.outputsOfType<BillingState>().isNotEmpty())
                "Input and output of Bill command should have the same linear ID." using (tx.outputsOfType<BillingState>().single().linearId == tx.inputsOfType<BillingState>().single().linearId)
                "Owner must be signer for Bill command." using (command.signers.toSet().contains(tx.outputsOfType<BillingState>().single().owner.owningKey))
            }
        }
    }
}