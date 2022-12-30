package kr.lostwar.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

operator fun <T : Any> Collection<T>.plus(collection: Collection<T>): List<T>{
    val result = ArrayList<T>(size + collection.size)
    result.addAll(this)
    result.addAll(collection)
    return result
}
operator fun <T : Any> T.plus(collection: Collection<T>): List<T>{
    val result = ArrayList<T>(collection.size + 1)
    result.add(this)
    result.addAll(collection)
    return result
}

fun <K : Any, V : Any> MutableMap<K, V>.mergeAll(map: Map<K, V>, mappingFunction: (V, V) -> V?) {
    map.forEach { (key, value) ->
        merge(key, value, mappingFunction)
    }
}

val UUID.onlinePlayer: Player?
    get() = Bukkit.getPlayer(this)

val Int.ticks: Duration; get() = (this * 50).milliseconds
val Long.ticks: Duration; get() = (this * 50).milliseconds

val Duration.inTicks: Long
    get() = inWholeMilliseconds / 50
val Duration.inTicksInt: Int; get() = inTicksInt.toInt()

val Duration.java: java.time.Duration
    get() = toJavaDuration()


private val armorStandOffsetMap = mapOf<EquipmentSlot, Vector>(
    EquipmentSlot.HAND to Vector(+0.3125, 1.375, 0.0),
    EquipmentSlot.OFF_HAND to Vector(-0.3125, 1.375, 0.0),
    EquipmentSlot.HEAD to Vector(0.0, 1.4375, 0.0),
)
val EquipmentSlot.armorStandOffset: Vector; get() = armorStandOffsetMap[this] ?: Vector()