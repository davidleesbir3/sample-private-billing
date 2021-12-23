package com.cdlexample.contracts

import com.cdlexample.states.RateCardState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class RateCardContract: Contract {
    companion object {
        const val ID = "com.cdlexample.contracts.RateCardContract"
    }

    interface Commands : CommandData {
        class Issue : Commands
        class Update : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val inputs = tx.inputs
        val outputs = tx.outputs
        when (command.value) {
            is Commands.Issue -> requireThat {
                "There should be no input for Issue command." using (inputs.isEmpty())
                "There should be exactly one RateCardState in the output of Issue command" using ( outputs.size == 1 && tx.outputsOfType<RateCardState>().isNotEmpty())
                "Operator must be signer for Issue command." using (command.signers.toSet().contains(tx.outputsOfType<RateCardState>().single().operator.owningKey))


            }
            is Commands.Update -> requireThat {
                "There should be exactly one RateCardState in the input for Update command." using (inputs.size == 1 && tx.inputsOfType<RateCardState>().isNotEmpty())
                "There should be exactly one RateCardState in the output for the Update command." using ( outputs.size == 1 && tx.outputsOfType<RateCardState>().isNotEmpty())
                "Input and output should have the same linear ID." using (tx.outputsOfType<RateCardState>().single().linearId == tx.inputsOfType<RateCardState>().single().linearId)
                "Operator must be signer for Update command." using (command.signers.toSet().contains(tx.outputsOfType<RateCardState>().single().operator.owningKey))
            }
        }
    }
}