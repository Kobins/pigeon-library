package kr.lostwar.util.math

import kr.lostwar.util.math.Matrix3x3.Companion.timesAssign
import kr.lostwar.util.math.VectorUtil.normalized
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// https://kobins.notion.site/Quaternion-9e416116aa8e4a049d4d9eee14284575
object RotationUtil {

    val EulerAngle.pitchInDegrees; get() = x.toDegrees()
    val EulerAngle.yawInDegrees; get() = y.toDegrees()
    val EulerAngle.rollInDegrees; get() = z.toDegrees()

    val EulerAngle.pitchInRadians; get() = x
    val EulerAngle.yawInRadians; get() = y
    val EulerAngle.rollInRadians; get() = z

    fun EulerAngle.getRotatedVector(v: Vector): Vector {
        val matrix = getRotationMatrix()
        return matrix * v
    }
    fun EulerAngle.rotateVector(v: Vector) {
        val matrix = getRotationMatrix()
        v *= matrix
    }

    fun EulerAngle.getRotationMatrix(): Matrix3x3 {
        val pitch = pitchInRadians
        val yaw = -yawInRadians
        val roll = -rollInRadians
        val cosalpha = cos(pitch); val cosbeta = cos(yaw); val cosgamma = cos(roll)
        val sinalpha = sin(pitch); val sinbeta = sin(yaw); val singamma = sin(roll)

        val x = Vector(
            cosgamma * cosbeta,
            singamma * cosbeta,
            -sinbeta
        )
        val y = Vector(
            cosgamma * sinbeta * sinalpha - singamma * cosalpha,
            singamma * sinbeta * sinalpha + cosgamma * cosalpha,
            cosbeta * sinbeta
        )
        val z = Vector(
            cosgamma * sinbeta * cosalpha + singamma * sinalpha,
            singamma * sinbeta * cosalpha - cosgamma * sinalpha,
            cosbeta * cosalpha
        )
        return Matrix3x3(x, y, z)
    }

    fun getRotationMatrix(forward: Vector, up: Vector = VectorUtil.UP): Matrix3x3 {
        val localZ = forward.normalized
        // 주어진 업벡터와 거의 일치하는 경우 임의의 x축을 제공
        val localX = if (abs(localZ.dot(up)) >= (1.0 - smallNumber)) {
            VectorUtil.RIGHT // 근데 이게 맞는지는 모르겠다 ㅎㅎ;
        } else {
            up.getCrossProduct(localZ).normalize()
        }
        val localY = localZ.getCrossProduct(localX).normalize()
        return Matrix3x3(localX, localY, localZ)
    }
}