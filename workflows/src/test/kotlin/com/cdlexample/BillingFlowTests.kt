package com.cdlexample

import com.cdlexample.flows.IssueBillingFlow
import com.cdlexample.flows.IssueRateCard
import com.cdlexample.flows.ProposeResponderFlow
import com.cdlexample.states.BillingState
import com.cdlexample.states.RateCardState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingFlowTests {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    private val network = MockNetwork(
        MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("com.cdlexample.contracts"),
        TestCordapp.findCordapp("com.cdlexample.flows")
    ))
    )
    private val a = network.createNode()
    private val b = network.createNode()
    private val operator = network.createNode()

    init {
        listOf(a, b).forEach {
            it.registerInitiatedFlow(ProposeResponderFlow::class.java)
        }
    }

    val partyA = a.info.legalIdentities.first()
    val partyB = b.info.legalIdentities.first()
    val partyOperator = operator.info.legalIdentities.first()


    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Operator issues a billing to A`() {
        val flow = IssueBillingFlow(partyA)
        val result: SignedTransaction = operator.startFlow(flow).runAndGet(network)

        // Check output state is returned
        val criteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        val billingsOnOperator: List<StateAndRef<BillingState>> = operator.services.vaultService.queryBy<BillingState>(criteria = criteria).states
        val billingsOnPartyA: List<StateAndRef<BillingState>> = a.services.vaultService.queryBy<BillingState>(criteria = criteria).states

        assertEquals(1, billingsOnOperator.size)
        assertEquals(1, billingsOnPartyA.size)
        assertEquals(billingsOnOperator.single().state.data.owner.owningKey, billingsOnPartyA.single().state.data.owner.owningKey)
    }
}