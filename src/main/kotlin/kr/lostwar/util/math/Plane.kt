package kr.lostwar.util.math

import kr.lostwar.util.math.VectorUtil.UP
import kr.lostwar.util.math.VectorUtil.getBukkitVector
import kr.lostwar.util.math.VectorUtil.minus
import kr.lostwar.util.math.VectorUtil.normalized
import kr.lostwar.util.math.VectorUtil.plus
import kr.lostwar.util.math.VectorUtil.times
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class Plane(
    val name: String,
    origin: Vector,
    normal: Vector,
) {
    var origin: Vector = origin; private set
    private var internalNormal: Vector = normal.normalized
        set(value) {
            field = value.normalized
            internalRight = UP.getCrossProduct(internalNormal).normalize()
            internalUp = normal.getCrossProduct(internalRight).normalize()
        }
    private var internalRight = UP.getCrossProduct(internalNormal).normalize()
    private var internalUp = normal.getCrossProduct(internalRight).normalize()
    var normal: Vector; get() = internalNormal.clone(); set(value) { internalNormal = value }
    val right: Vector; get() = internalRight.clone()
    val up: Vector; get() = internalUp.clone()

    fun worldToLocal(world: Vector): Vector? {
        val normal = internalNormal
        // 평면 위의 점이 아니면
//        if(abs(normal.dot(origin) - normal.dot(vector3)) > 0.01) {
//            return null
//        }
        val relativeVector = world - origin
        val z = normal.dot(relativeVector)
        return Vector(internalRight.dot(relativeVector), internalUp.dot(relativeVector), z)
    }
    fun localToWorld(local: Vector): Vector {
        val right = internalRight
        val up = internalUp
        val normal = internalNormal
        return Vector(
            right.x * local.x + up.x * local.y + normal.x * local.z + origin.x,
            right.y * local.x + up.y * local.y + normal.y * local.z + origin.y,
            right.z * local.x + up.z * local.y + normal.z * local.z + origin.z,
        )
    }
    // https://rito15.github.io/posts/raycast-to-plane/
    fun raycast(origin: Vector, ray: Vector): Vector? {
        val direction = ray.normalized
        val d = internalNormal.dot(this.origin - origin) / internalNormal.dot(direction)

        // 레이 방향이 평면을 향하지 않는 경우
        if(d < 0) return null

        val hit = origin + direction * d

        val sqrRay = ray.lengthSquared()
        val sqrHit = (hit - origin).lengthSquared()

        // 길이가 짧은 경우
        if(sqrRay < sqrHit) return null

        return hit
    }

    companion object {
        fun ConfigurationSection.getPlane(key: String): Plane? {
            val origin = getBukkitVector("$key.origin") ?: return null
            val normal = getBukkitVector("$key.normal") ?: return null
            return Plane(key, origin, normal)
        }
    }
}