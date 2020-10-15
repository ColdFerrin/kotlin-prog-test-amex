package org.example

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class OrdersTest {
    lateinit var mail: Channel<Int>

    @Before fun init() {
        mail = Channel<Int>()
    }

    @Test fun `apple total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("apple")))
        val total = mail.receive()
        assertEquals(60, total)
        orders.close()
    }

    @Test fun `2 apple total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("apple", "apple")))
        val total = mail.receive()
        assertEquals(60, total)
        orders.close()
    }

    @Test fun `3 apple total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("apple", "apple", "apple")))
        val total = mail.receive()
        assertEquals(120, total)
        orders.close()
    }

    @Test fun `orange total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("orange")))
        val total = mail.receive()
        assertEquals(25, total)
        orders.close()
    }

    @Test fun `2 orange total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("orange", "orange")))
        val total = mail.receive()
        assertEquals(50, total)
        orders.close()
    }

    @Test fun `3 orange total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("orange", "orange", "orange")))
        val total = mail.receive()
        assertEquals(50, total)
        orders.close()
    }

    @Test fun `4 orange total`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("orange", "orange", "orange", "orange")))
        val total = mail.receive()
        assertEquals(75, total)
        orders.close()
    }

    @Test fun `Test service with apple apple orange apple sequence`(): Unit = runBlocking {
        val orders = ordersActor(mail)
        orders.send(NewOrder(listOf("apple", "apple", "orange", "apple")))
        val total = mail.receive()
        assertEquals(145, total)
        orders.close()
    }

}
