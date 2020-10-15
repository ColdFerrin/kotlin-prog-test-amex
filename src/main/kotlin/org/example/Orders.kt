package org.example

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.confluent.kafka.serializers.KafkaAvroSerializer
import kafka.utils.json.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import org.json.JSONObject
import java.net.InetAddress
import java.util.*
import java.util.concurrent.Future
import kotlin.math.floor


sealed class Orders
class NewOrder(val items: List<String>) : Orders()
class SetStock(val apples: Int, val oranges: Int) : Orders()

data class OrderSubmitted(
    val Message: String
)

fun CoroutineScope.ordersActor(mail: SendChannel<String>) = actor<Orders> {
    val stock: MutableMap<String, Int> = mutableMapOf()
    for (msg in channel) {
        when (msg) {
            is NewOrder -> {
                var numApples = 0
                var numOranges = 0
                msg.items.forEach {
                    when (it) {
                        "apple" -> ++numApples
                        "orange" -> ++numOranges
                        else -> {
                        }
                    }
                }

                val outputString = StringBuilder()
                var applesRemaining = stock.getOrDefault("apples", 0)
                var orangesRemaining = stock.getOrDefault("oranges", 0)
                if (numApples < applesRemaining && numOranges < orangesRemaining) {
                    outputString.append("The total is " + getTotal(numApples, numOranges))
                    applesRemaining -= numApples
                    orangesRemaining -= numOranges
                    stock["apples"] = applesRemaining
                    stock["oranges"] = orangesRemaining
                } else {
                    outputString.append("Error: ")

                    if (numApples > applesRemaining)
                        outputString.append("Apples Remaining $applesRemaining ")

                    if (numOranges > orangesRemaining)
                        outputString.append("Oranges remaining $orangesRemaining")
                }

                mail.send(outputString.toString())
            }
            is SetStock -> {
                stock["apples"] = (stock.getOrDefault("apples", 0) + msg.apples)
                stock["oranges"] = (stock.getOrDefault("oranges", 0) + msg.oranges)
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

private fun createProducer(brokers: String): Producer<String, String> {
    val props = Properties()
    props["bootstrap.servers"] = brokers
    props["client.id"] = InetAddress.getLocalHost().getHostName()
    props["acks"] = "all"
    props["key.serializer"] = StringSerializer::class.java.canonicalName
    props["value.serializer"] = StringSerializer::class.java.canonicalName
    props["key.converter.schema.registry.url"] = "http://localhost:8081"
    props["value.converter.schema.registry.url"] = "http://localhost:8081"
    props["schema.registry.url"] = "http://localhost:8081"
    return KafkaProducer<String, String>(props)
}

fun main(): Unit = runBlocking {
    val producer = createProducer("localhost:9092")
    val jsonMapper = ObjectMapper().apply {
        registerKotlinModule()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        setDateFormat(StdDateFormat())
    }


    val mail = Channel<String>()

    GlobalScope.launch {
        mail.consumeEach {
            val orderMsg = OrderSubmitted(it)
            val future: Future<RecordMetadata> = producer.send(ProducerRecord("order-submitted", jsonMapper.writeValueAsString(orderMsg)))
            val metadata = future.get()
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

        println("Do you want to run again (Y/N): ")
        val input = readLine()
        if(input == "n" || input == "N"){
            notDone = false
        }


    }

    orders.close()
}

