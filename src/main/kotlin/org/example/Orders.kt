package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlin.math.floor

sealed class Orders
class NewOrder(val items: List<String>) : Orders()

fun CoroutineScope.ordersActor(mail: SendChannel<Int>) = actor<Orders> {
    for (msg in channel) {
        when (msg) {
            is NewOrder -> {
                var numApples = 0
                var numOranges = 0
                msg.items.forEach {
                    when (it) {
                        "apple" -> ++numApples
                        "orange" -> ++numOranges
                        else -> {}
                    }
                }

                mail.send(getTotal(numApples, numOranges))
            }
        }
    }
}

fun getTotal(apples: Int, oranges: Int): Int {
    var total = 0
    total += ((floor(apples.toDouble() / 2).toInt() + (apples % 2)) * 60)
    total += (((floor(oranges.toDouble() / 3).toInt() * 2) + (oranges % 3)) * 25)

    return total
}

fun main(): Unit = runBlocking {
    val mail = Channel<Int>()

    GlobalScope.launch {
        mail.consumeEach {
            println("The total is $it")
        }
    }

    val orders = ordersActor(mail)
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

        orders.send(NewOrder(items.toList()))
        val response = CompletableDeferred<Int?>()

        println("Do you want to run again (Y/N): ")
        val input = readLine()
        if(input == "n" || input == "N"){
            notDone = false
        }


    }

    orders.close()
}

