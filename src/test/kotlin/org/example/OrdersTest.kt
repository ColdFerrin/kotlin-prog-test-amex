package org.example

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class OrdersTest {

    @Test fun `apple total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("apple")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(60, total)
        orders.close()
    }

    @Test fun `2 apple total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("apple", "apple")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(60, total)
        orders.close()
    }

    @Test fun `3 apple total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("apple", "apple", "apple")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(120, total)
        orders.close()
    }

    @Test fun `orange total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("orange")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(25, total)
        orders.close()
    }

    @Test fun `2 orange total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("orange", "orange")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(50, total)
        orders.close()
    }

    @Test fun `3 orange total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("orange", "orange", "orange")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(50, total)
        orders.close()
    }

    @Test fun `4 orange total`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("orange", "orange", "orange", "orange")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(75, total)
        orders.close()
    }

    @Test fun `Test service with apple apple orange apple sequence`(): Unit = runBlocking {
        val orders = ordersActor()
        orders.send(newOrder(listOf("apple", "apple", "orange", "apple")))
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        val total = response.await()
        assertEquals(145, total)
        orders.close()
    }

}
