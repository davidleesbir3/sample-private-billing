package com.cdlexample

import com.cdlexample.flows.IssueRateCard
import com.cdlexample.flows.ProposeFlow
import com.cdlexample.flows.ProposeResponderFlow
import com.cdlexample.flows.Responder
import com.cdlexample.states.AgreementState
import com.cdlexample.states.RateCardState
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.contextLogger
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.log
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
import java.util.*
import kotlin.test.assertEquals

class RateCardFlowTests {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("com.cdlexample.contracts"),
        TestCordapp.findCordapp("com.cdlexample.flows")
    )))
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
    fun `Issue a rate card`() {
        val flow = IssueRateCard(rate = 100)
        val result: SignedTransaction = operator.startFlow(flow).runAndGet(network)

        // Check output state is returned
        val outputState = result.coreTransaction.outputStates.single() as RateCardState
        assertEquals(100, outputState.rate, "Rate should be correct")
    }

    @Test
    fun `Only one rate card can be issued`() {
        val flow1 = IssueRateCard(rate = 100)
        operator.startFlow(flow1)
        network.runNetwork()

        expectedException.expectCause(Matchers.any(IllegalArgumentException::class.java))
        val flow2 = IssueRateCard(rate = 200)
        val future = operator.startFlow(flow2)

        network.runNetwork()
        expectedException.expectCause(Matchers.any(IllegalArgumentException::class.java))
        future.get()
    }
}