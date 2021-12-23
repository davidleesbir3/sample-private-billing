package com.cdlexample.states

import com.cdlexample.contracts.BillingContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(BillingContract::class)
data class BillingState (val owner: Party,
                         val operator: Party,
                         val cumulativeUse: Long,
                         override val participants: List<AbstractParty> = listOf(owner, operator),
                         override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState