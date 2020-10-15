package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlin.math.floor

sealed class Orders
class newOrder(val items: List<String>) : Orders()
class getTotal(val response: CompletableDeferred<Int?>) : Orders()

fun CoroutineScope.ordersActor() = actor<Orders> {
    val totals = mutableListOf<Int>()
    for (msg in channel) {
        when (msg) {
            is newOrder -> {
                var numApples = 0
                var numOranges = 0
                msg.items.forEach {
                    when (it) {
                        "apple" -> ++numApples
                        "orange" -> ++numOranges
                        else -> {}
                    }
                }

                totals.add(getTotal(numApples, numOranges))
            }
            is getTotal -> msg.response.complete(totals.removeFirstOrNull())
        }
    }
}

fun getTotal(apples: Int, oranges: Int): Int {
    var total = 0
    total += ((floor(apples.toDouble() / 2).toInt() + (apples % 2)) * 60)
    total += (((floor(oranges.toDouble() / 3).toInt() * 2) + (oranges % 3)) * 25)

    return total
}

fun main() = runBlocking<Unit> {
    val orders = ordersActor()
    var notDone = true
    while (notDone) {
        val items = mutableListOf<String>()
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
        val response = CompletableDeferred<Int?>()
        orders.send(getTotal(response))
        println("total = ${response.await()}")

        println("Do you want to run again (Y/N): ")
        val input = readLine()
        if(input == "n" || input == "N"){
            notDone = false
        }
    }

    orders.close()
}

