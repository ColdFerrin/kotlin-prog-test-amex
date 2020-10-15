package org.example

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals

class HelloTest {

    @Test fun `Test service with apple apple orange apple sequence`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("apple", "apple", "orange", "apple")))
        val response = CompletableDeferred<Int>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(205, total)
        orders.close()
    }

}
