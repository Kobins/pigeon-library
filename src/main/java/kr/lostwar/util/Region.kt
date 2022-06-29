package kr.lostwar.util

import kr.lostwar.util.math.VectorUtil.toVectorString
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min

data class Region(
    val x: IntRange,
    val y: IntRange,
    val z: IntRange,
) {
    constructor(
        x1: Int, y1: Int, z1: Int,
        x2: Int, y2: Int, z2: Int,
    ) : this(
        min(x1, x2) .. max(x1, x2),
        min(y1, y2) .. max(y1, y2),
        min(z1, z2) .. max(z1, z2),
    )
    constructor(v1: Vector, v2: Vector) : this(v1.blockX, v1.blockY, v1.blockZ, v2.blockX, v2.blockY, v2.blockZ)


    val min: Vector; get() = Vector(x.first, y.first, z.first)
    val max: Vector; get() = Vector(x.last, y.last, z.last)

    fun expanded(amount: Int): Region {
        return Region(
            x.first - amount, y.first - amount, z.first - amount,
            x.last + amount, y.last + amount, z.last + amount,
        )
    }

    operator fun contains(vector: Vector): Boolean {
        return vector.blockX in x && vector.blockY in y && vector.blockZ in z
    }
    operator fun contains(location: Location): Boolean {
        return location.blockX in x && location.blockY in y && location.blockZ in z
    }
    operator fun contains(entity: Entity): Boolean {
        return contains(entity.location)
    }
    operator fun contains(block: Block): Boolean{
        return contains(block.location)
    }

    override fun equals(other: Any?): Boolean {
        if(other is Region){
            return     x.first == other.x.first
                    && y.first == other.y.first
                    && z.first == other.z.first
                    && x.last == other.x.last
                    && y.last == other.y.last
                    && z.last == other.z.last
        }
        return false
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    companion object {
        operator fun Iterable<Region>.contains(vector: Vector): Boolean {
            for(region in this){
                if(vector in region){
                    return true
                }
            }
            return false
        }
        operator fun Iterable<Region>.contains(location: Location): Boolean {
            for(region in this){
                if(location in region){
                    return true
                }
            }
            return false
        }
        operator fun Iterable<Region>.contains(entity: Entity): Boolean {
            return contains(entity.location)
        }
        operator fun Iterable<Region>.contains(block: Block): Boolean {
            return contains(block.location)
        }

        fun ConfigurationSection.getRegion(dir: String, def: Region = Region(0, 0, 0, 0, 0, 0)): Region {
            val minRaw = getString("$dir.min") ?: return def
            val minArray = minRaw.split(',').map { it.trim().toIntOrNull() }
            val minX = minArray.getOrNull(0) ?: def.x.first
            val minY = minArray.getOrNull(1) ?: def.y.first
            val minZ = minArray.getOrNull(2) ?: def.z.first

            val maxRaw = getString("$dir.max") ?: return def
            val maxArray = maxRaw.split(',').map { it.trim().toIntOrNull() }
            val maxX = maxArray.getOrNull(0) ?: def.x.last
            val maxY = maxArray.getOrNull(1) ?: def.y.last
            val maxZ = maxArray.getOrNull(2) ?: def.z.last

            return Region(minX, minY, minZ, maxX, maxY, maxZ)
        }
        fun ConfigurationSection.setRegion(dir: String, region: Region) {
            set("$dir.min", region.min.toVectorString())
            set("$dir.max", region.max.toVectorString())
        }
    }

}