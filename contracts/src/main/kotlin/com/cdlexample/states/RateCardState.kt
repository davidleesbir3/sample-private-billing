package com.cdlexample.states

import com.cdlexample.contracts.RateCardContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(RateCardContract::class)
data class RateCardState (val operator: Party,
                          val rate: Long,
                          override val participants: List<AbstractParty> = listOf(operator),
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState