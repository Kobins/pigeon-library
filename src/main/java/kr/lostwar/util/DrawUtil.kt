package kr.lostwar.util

import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.math.VectorUtil.div
import kr.lostwar.util.math.VectorUtil.plus
import kr.lostwar.util.math.lerp
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.lang.Integer.max

object DrawUtil {

    fun drawFor(duration: Long, period: Long, list: List<Vector>, options: DustOptions, receivers: Collection<Player> = Bukkit.getOnlinePlayers()) {
        object : BukkitRunnable() {
            var count = duration / period
            override fun run() {
                if(count < 0){
                    cancel()
                    return
                }
                drawPoints(list, options, receivers)
                --count
            }
        }.runTaskTimerAsynchronously(PigeonLibraryPlugin.instance, 0L, period)
    }

    fun getDoubleRange(start: Double, end: Double, divide: Int): List<Double> {
        if(divide <= 1) return listOf(start, end)
        val divideDouble = divide.toDouble()
        return (0..divide).map { lerp(start, end, it / divideDouble) }
    }

    fun getRay(origin: Vector, direction: Vector, divide: Int): List<Vector> {
        val list = ArrayList<Vector>()
        val move = direction / max(2, divide).toDouble()
//    println("origin: ${origin.toVectorString()}")
//    println("direction: ${direction.toVectorString()} (length: ${direction.length()})")
//    println("divide: $divide")
//    println("move: ${move.toVectorString()} (length: ${move.length()})")
        var point = origin.clone()
        for(i in 0..divide) {
            list.add(point)
            point += move
        }
//    println("list:")
//    list.forEachIndexed { index, vector ->
//        println("[$index]: ${vector.toVectorString()}")
//    }
        return list
    }

    fun getSingleBlock(block: Vector, divide: Int): List<Vector> {
        val range01 = getDoubleRange(0.0, 1.0, divide)
        val blockX = block.blockX.toDouble()
        val blockY = block.blockY.toDouble()
        val blockZ = block.blockZ.toDouble()

        val list = ArrayList<Vector>()
        //up and down
        for(y in 0..1){
            for(z in 0..1) {
                for(x in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
                for(x in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
            }
            for(x in 0..1){
                for(z in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
                for(z in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
            }
        }
        //vertical
        for(x in 0..1){
            for(z in 0..1){
                for(y in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
                for(y in range01) list.add(Vector(blockX + x, blockY + y, blockZ + z))
            }
        }
        return list.distinct()
    }

    operator fun DustOptions.component1() = color
    operator fun DustOptions.component2() = size

    fun drawPoints(
        list: Iterable<Vector>,
        options: DustOptions,
        receivers: Collection<Player> = Bukkit.getOnlinePlayers()
    ) {
        val (color, size) = options
        list.forEach { vector ->
            receivers.forEach { player ->
                val location = vector.toLocation(player.world)
                player.spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, DustOptions(color, size))
            }
        }

    }

}