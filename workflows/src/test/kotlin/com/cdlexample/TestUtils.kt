package com.cdlexample

import net.corda.core.concurrent.CordaFuture
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork

fun <V> CordaFuture<V>.runAndGet(network: MockNetwork): V {
    network.runNetwork()
    return this.getOrThrow()
}