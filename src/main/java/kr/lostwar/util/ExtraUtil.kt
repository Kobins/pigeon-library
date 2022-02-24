package kr.lostwar.util

import net.kyori.adventure.text.format.TextColor
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

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

val IntRange.size: Int
    get() = endInclusive - start + 1
fun ClosedFloatingPointRange<Double>.lerp(t: Double) = lerp(start, endInclusive, (t.clamp(0.0..1.0)))
fun ClosedFloatingPointRange<Double>.center() = lerp(0.5)
fun ClosedFloatingPointRange<Double>.random() = lerp(Random.nextDouble())

val UUID.onlinePlayer: Player?
    get() = Bukkit.getPlayer(this)

fun TextColor.toBukkitColor(): org.bukkit.Color {
    return org.bukkit.Color.fromRGB(value())
}

fun <K : Any, V : Any> MutableMap<K, V>.mergeAll(map: Map<K, V>, mappingFunction: (V, V) -> V?) {
    map.forEach { (key, value) ->
        merge(key, value, mappingFunction)
    }
}

fun Player.getExpToDrop(block: Block): Int {
    val pos = BlockPos(block.x, block.y, block.z)
    val level = (world as CraftWorld).handle
    val nmsData = level.getBlockState(pos)
    val nmsBlock = nmsData.block ?: return 0

    val item = inventory.itemInMainHand

    if (block.isPreferredTool(item)) {
        return nmsBlock.getExpDrop(nmsData, level, pos, CraftItemStack.asNMSCopy(item))
    }
    return 0

}

val BlockFace.simpleFace: BlockFace
    get() = when(this) {
        BlockFace.UP -> BlockFace.UP
        BlockFace.DOWN -> BlockFace.DOWN
        BlockFace.EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST -> BlockFace.EAST
        BlockFace.WEST, BlockFace.WEST_NORTH_WEST, BlockFace.WEST_SOUTH_WEST -> BlockFace.WEST
        BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST -> BlockFace.SOUTH
        BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST -> BlockFace.NORTH
        else -> BlockFace.UP
    }

val BlockFace.left: BlockFace?
    get() = when(this) {
        BlockFace.NORTH -> BlockFace.WEST
        BlockFace.WEST -> BlockFace.SOUTH
        BlockFace.SOUTH -> BlockFace.EAST
        BlockFace.EAST -> BlockFace.NORTH
        else -> null
    }
val BlockFace.right: BlockFace?
    get() = when(this) {
        BlockFace.NORTH -> BlockFace.EAST
        BlockFace.WEST -> BlockFace.NORTH
        BlockFace.SOUTH -> BlockFace.WEST
        BlockFace.EAST -> BlockFace.SOUTH
        else -> null
    }