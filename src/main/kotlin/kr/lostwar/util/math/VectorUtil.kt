package kr.lostwar.util.math

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.EulerAngle
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import kotlin.math.*

object VectorUtil {
    private val pihalf = PI/2.0
    private val pi = PI
    private val pi2 = 6.283185307179586

    val ZERO: Vector; get() = Vector(0, 0, 0)
    val UP: Vector; get() = Vector(0, 1, 0)
    val DOWN: Vector; get() = Vector(0, -1, 0)
    val LEFT: Vector; get() = Vector(-1, 0, 0)
    val RIGHT: Vector; get() = Vector(1, 0, 0)
    val FORWARD: Vector; get() = Vector(0, 0, 1)
    val BACK: Vector; get() = Vector(0, 0, -1)

    val Vector.yaw: Double
        get() {
            return if (x == 0.0 && z == 0.0) 0.0
            else (atan2(-x, z) + pi2) % pi2
        }
    val Vector.yawDegree: Double
        get() = Math.toDegrees(yaw)
    val Vector.yawDegreeFloat: Float
        get() = yawDegree.toFloat()

    val Vector.pitch: Double
        get() {
            return if (x == 0.0 && z == 0.0) {
                if (y > 0.0) -pihalf else pihalf
            } else (atan(-y / sqrt(x * x + z * z)))
        }
    val Vector.pitchDegree: Double
        get() = Math.toDegrees(pitch)
    val Vector.pitchDegreeFloat: Float
        get() = pitchDegree.toFloat()

    fun Vector.toYawPitchDegree(): Pair<Float, Float>{
        return Pair(yawDegreeFloat, pitchDegreeFloat)
    }
    fun Vector.toYawPitch(): Pair<Float, Float>{
        return Pair(yaw.toFloat(), pitch.toFloat())
    }
    fun fromYawPitch(yaw: Float, pitch: Float) = fromYawPitch(yaw.toDouble(), pitch.toDouble())
    fun fromYawPitch(yaw: Double, pitch: Double) = Vector(0, 0, 1).rotateAroundX(pitch).rotateAroundY(-yaw)

    fun lerp(start: Vector, end: Vector, t: Double) = start.clone().multiply(1-t).add(end.clone().multiply(t))
    fun lerp(start: Location, end: Location, t: Double) = start.clone().multiply(1-t).add(end.clone().multiply(t))

    fun Location.getDisplayString() = String.format("%.3f, %.3f, %.3f", x, y, z)
    fun Vector.getDisplayString() = String.format("%.3f, %.3f, %.3f", x, y, z)

    fun fromVectorString(string: String?): Vector? {
        if(string == null){
            return null
        }
        val split = string.split(',').map { it.trim() }
        if(split.size < 3) return null
        val x = split[0].toDoubleOrNull() ?: return null
        val y = split[1].toDoubleOrNull() ?: return null
        val z = split[2].toDoubleOrNull() ?: return null
        return Vector(x, y, z)
    }


    fun Location.toVectorString(): String{
        return "$x, $y, $z"
    }
    fun Location.toLocationString(): String{
        return "$x, $y, $z, $yaw, $pitch"
    }
    fun Vector.toVectorString(): String{
        return "$x, $y, $z"
    }

    fun Vector.toLocationString(direction: Vector): String{
        return "${toVectorString()}, ${direction.yawDegreeFloat}, ${direction.pitchDegreeFloat}"
    }

    fun fromLocationString(string: String?, world: World? = null): Location {
        if(string == null){
            return Location(world, 0.0, 64.0, 0.0)
        }
        val split = string.split(',').map { it.trim() }
        val x = split.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val y = split.getOrNull(1)?.toDoubleOrNull() ?: 64.0
        val z = split.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        val yaw = split.getOrNull(3)?.toFloatOrNull() ?: 0f
        val pitch = split.getOrNull(4)?.toFloatOrNull() ?: 0f
//    println("fromLocationString($string) -> $x, $y, $z, $yaw, $pitch")
        return Location(world, x, y, z, yaw, pitch)
    }
    fun fromLocationStringOrNull(string: String?, world: World? = null): Location? {
        if(string == null){
            return null
        }
        val split = string.split(',').map { it.trim() }
        val x = split.getOrNull(0)?.toDoubleOrNull() ?: return null
        val y = split.getOrNull(1)?.toDoubleOrNull() ?: return null
        val z = split.getOrNull(2)?.toDoubleOrNull() ?: return null
        val yaw = split.getOrNull(3)?.toFloatOrNull() ?: return null
        val pitch = split.getOrNull(4)?.toFloatOrNull() ?: return null
//    println("fromLocationString($string) -> $x, $y, $z, $yaw, $pitch")
        return Location(world, x, y, z, yaw, pitch)
    }

    val Vector.normalized: Vector
        get() = clone().normalize()
    operator fun Location.plus(other: Vector) = Location(world, x + other.x, y + other.y, z + other.z)
    operator fun Location.plus(other: Location) = Location(world, x + other.x, y + other.y, z + other.z)
    operator fun Vector.plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
    operator fun Vector.plus(other: Location) = Vector(x + other.x, y + other.y, z + other.z)
    operator fun Location.minus(other: Vector) = Location(world, x - other.x, y - other.y, z - other.z)
    operator fun Location.minus(other: Location) = Location(world, x - other.x, y - other.y, z - other.z)
    operator fun Vector.minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
    operator fun Vector.minus(other: Location) = Vector(x - other.x, y - other.y, z - other.z)
    operator fun Vector.times(scala: Double) = Vector(x * scala, y * scala, z * scala)
    operator fun Vector.times(scala: Int) = times(scala.toDouble())
    operator fun Vector.div(scala: Double) = Vector(x / scala, y / scala, z / scala)
    operator fun Vector.unaryMinus() = times(-1.0)

    fun Vector.dot(location: Location) = x * location.x + y * location.y + z * location.z

    fun Location.modifiedX(x: Double) = Location(world, x, y, z, yaw, pitch)
    fun Location.modifiedY(y: Double) = Location(world, x, y, z, yaw, pitch)
    fun Location.modifiedZ(z: Double) = Location(world, x, y, z, yaw, pitch)
    fun Vector.modifiedX(x: Double) = Vector(x, y, z)
    fun Vector.modifiedY(y: Double) = Vector(x, y, z)
    fun Vector.modifiedZ(z: Double) = Vector(x, y, z)

    fun Vector.distanceRectilinear(other: Vector) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    fun Vector.distanceRectilinear(other: Location) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    fun Location.distanceRectilinear(other: Location) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    fun Location.distanceRectilinear(other: Vector) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)


    fun Vector.invertByNormalFace(normal: BlockFace): Vector {
        return when(normal) {
            BlockFace.NORTH, BlockFace.SOUTH -> Vector(x, y, -z)
            BlockFace.EAST, BlockFace.WEST -> Vector(-x, y, z)
            BlockFace.UP, BlockFace.DOWN -> Vector(x, -y, z)
            else -> Vector(x, y, z)
        }
    }

    fun Vector.makeSafe(): Vector {
        if(!NumberConversions.isFinite(x)){
            x = 0.0
        }
        if(!NumberConversions.isFinite(y)){
            y = 0.0
        }
        if(!NumberConversions.isFinite(z)){
            z = 0.0
        }
        return this
    }

    fun ConfigurationSection.getBukkitVector(dir: String?): Vector?{
        if(dir == null) return null
        val obj = get(dir)
        if(obj is String) {
            val split = obj.split(',').map { it.trim() }
            if(split.size < 3) return null
            val x = split[0].toDoubleOrNull() ?: return null
            val y = split[1].toDoubleOrNull() ?: return null
            val z = split[2].toDoubleOrNull() ?: return null
            return Vector(x, y, z)
        }
        if(obj is ConfigurationSection) {
            val x = obj.getDouble("x")
            val y = obj.getDouble("y")
            val z = obj.getDouble("z")
            return Vector(x, y, z)
        }
        return null
    }

    fun Vector.localToWorld(forward: Vector): Vector {
        val right = forward.getCrossProduct(UP)
        val up = right.getCrossProduct(forward)
        return (forward * z).add(right * x).add(up * y)
    }

    fun Vector.toEulerAngle(): EulerAngle = EulerAngle(x, y, z)

    fun Location.copy(
        world: World? = this.world,
        x: Double = this.x,
        y: Double = this.y,
        z: Double = this.z,
        yaw: Float = this.yaw,
        pitch: Float = this.pitch,
    ) = Location(world, x, y, z, yaw, pitch)
}