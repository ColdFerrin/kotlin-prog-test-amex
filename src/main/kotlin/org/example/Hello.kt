package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

sealed class Orders
class newOrder(val items: List<String>) : Orders()
class getTotal(val response: CompletableDeferred<Int>) : Orders()

fun CoroutineScope.ordersActor() = actor<Orders> {
    var total = 0
    for (msg in channel) {
        when (msg) {
            is newOrder -> {
                msg.items.forEach {
                    when (it) {
                        "apple" -> total += 60
                        "orange" -> total += 25
                        else -> {}
                    }
                }
            }
            is getTotal -> msg.response.complete(total)
        }
    }
}

fun main(args: Array<String>) = runBlocking<Unit> {
    val orders = ordersActor()
    var notDone = true
    while (notDone) {
        var items = mutableListOf<String>()
        var moreItems = true
        while (moreItems) {
            println("Press 1 to add an apple, 2 to add an orange, or 0 for done: ")
            when (readLine()?.toInt()) {
                0 -> moreItems = false
                1 -> items.add("apple")
                2 -> items.add("orange")
                else -> println("Bad input")
            }
        }

        orders.send(newOrder(items.toList()))
        val response = CompletableDeferred<Int>()
        orders.send(getTotal(response))
        println("total = ${response.await()}")
        orders.close()
    }
}

