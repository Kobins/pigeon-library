package kr.lostwar.util.math

import kr.lostwar.util.math.RotationUtil.getRotationMatrix
import kr.lostwar.util.math.RotationUtil.pitchInRadians
import kr.lostwar.util.math.RotationUtil.rollInRadians
import kr.lostwar.util.math.RotationUtil.yawInRadians
import kr.lostwar.util.math.VectorUtil.get
import kr.lostwar.util.math.VectorUtil.normalized
import kr.lostwar.util.math.VectorUtil.set
import kr.lostwar.util.math.VectorUtil.standardBasisVectors
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.*

class Quaternion {

    var w: Double = 1.0
    val r: Vector = Vector(0.0, 0.0, 0.0)

    var x: Double; get() = r.x; set(value) { r.x = value }
    var y: Double; get() = r.y; set(value) { r.y = value }
    var z: Double; get() = r.z; set(value) { r.z = value }

    /**
     * (w, r=(x, y, z))를 사용해 사원수를 생성합니다.
     */
    constructor(w: Double, r: Vector) {
        this.w = w
        this.r.set(r)
    }

    /**
     * (w, x, y, z)를 그대로 사용해 사원수를 생성합니다.
     */
    constructor(w: Double, x: Double, y: Double, z: Double) {
        set(w, x, y, z)
    }

    fun set(w: Double, x: Double, y: Double, z: Double) {
        this.w = w
        this.r.set(x, y, z)
    }

    constructor(from: Quaternion) {
        setFromQuaternion(from)
    }

    fun setFromQuaternion(from: Quaternion) {
        w = from.w
        r.set(from.r)
    }

    /**
     * 축-각을 기반으로 사원수를 생성합니다.
     */
    constructor(axis: Vector, angleInDegrees: Double) {
        setFromAxisAngle(axis, angleInDegrees)
    }

    fun setFromAxisAngle(axis: Vector, angleInDegrees: Double) {
        val normalizedAxis = axis.normalized
        val angleInRadians = angleInDegrees.toRadians() * 0.5
        val sin = sin(angleInRadians)
        val cos = cos(angleInRadians)
        w = cos
        r.set(normalizedAxis).multiply(sin)
        normalize()
    }

    /**
     * 회전행렬 기반으로 사원수를 생성합니다.
     */
    constructor(matrix: Matrix3x3) {
        setFromMatrix(matrix)
    }

    fun setFromMatrix(matrix: Matrix3x3) {
        // 회전행렬이 아닌 경우 identity
        if(!equalsInTolerance(matrix[0].lengthSquared(), 1.0)
            || !equalsInTolerance(matrix[1].lengthSquared(), 1.0)
            || !equalsInTolerance(matrix[2].lengthSquared(), 1.0)
        ) {
            w = 1.0
            r.set(0.0, 0.0, 0.0)
            return
        }

        var root: Double
        val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]

        if(trace > 0.0) {
            root = sqrt(trace + 1.0)
            w = 0.5 * root
            root *= 2.0

            // w 요소를 구하고 나머지 X,Y,Z를 계산
            r.set(
                (matrix[1][2] - matrix[2][1]) * root,
                (matrix[2][0] - matrix[0][2]) * root,
                (matrix[0][1] - matrix[1][0]) * root,
            )
            return
        }

        var i = 0
        // x, y, z 중 가장 큰 요소 파악
        if(matrix[1][1] > matrix[0][0]) i = 1
        if(matrix[2][2] > matrix[i][i]) i = 2

        // i, j, k의 순서 지정
        val j = nextIndex[i]
        val k = nextIndex[j]

        // 가장 큰 요소의 값 구하기
        root = sqrt(matrix[i][i] - matrix[j][j] - matrix[k][k] + 1.0)

        r[i] = 0.5 * root
        root *= 2.0

        // 나머지 두 요소의 값 구하기
        r[j] = (matrix[i][j] + matrix[j][i]) * root
        r[k] = (matrix[i][k] + matrix[k][i]) * root

        w = (matrix[j][k] - matrix[k][j]) * root
    }

    constructor(forward: Vector, up: Vector = VectorUtil.UP) {
        setFromForward(forward, up)
    }

    fun setFromForward(forward: Vector, up: Vector = VectorUtil.UP) {
        setFromMatrix(getRotationMatrix(forward, up))
    }

    /**
     * 오일러 각을 기반으로 사원수를 생성합니다.
     */
    constructor(eulerAngle: EulerAngle) : this(eulerAngle.pitchInRadians, eulerAngle.yawInRadians, eulerAngle.rollInRadians)
    constructor(pitchInRadians: Double, yawInRadians: Double, rollInRadians: Double) {
        setFromEuler(pitchInRadians, yawInRadians, rollInRadians)
    }
    fun setFromEuler(eulerAngle: EulerAngle) = setFromEuler(eulerAngle.pitchInRadians, eulerAngle.yawInRadians, eulerAngle.rollInRadians)
    fun setFromEuler(pitchInRadians: Double, yawInRadians: Double, rollInRadians: Double) {
        // 사원수는 반각 사용함
        val halfPitch = pitchInRadians * 0.5
        val halfYaw = -yawInRadians * 0.5
        val halfRoll = -rollInRadians * 0.5

        val sp = sin(halfPitch); val sy = sin(halfYaw); val sr = sin(halfRoll)
        val cp = cos(halfPitch); val cy = cos(halfYaw); val cr = cos(halfRoll)

        w = cr * cp * cy + sr * sp * sy
        r.set(
            cr * sp * cy - sr * cp * sy,
            cr * cp * sy + sr * sp * cy,
            sr * cp * cy - cr * sp * sy,
        )
    }

    /**
     * 회전된 벡터를 구합니다.
     */
    fun getRotatedVector(v: Vector): Vector {
        val q = r.clone()
        val t = q.getCrossProduct(v).multiply(2.0)
        return q.crossProduct(t)
            .add(t.multiply(w))
            .add(v)
    }

    /**
     * 벡터를 회전시킵니다.
     */
    fun rotateVector(v: Vector): Vector {
        return v.set(getRotatedVector(v))
    }

    /**
     * 회전된 벡터를 구합니다.
     */
    operator fun times(v: Vector) = getRotatedVector(v)

    /**
     * 사원수 곱을 수행합니다.
     */
    operator fun times(q: Quaternion): Quaternion {
        val v1 = r.clone()
        val v2 = q.r.clone()
        val w = w * q.w - v1.dot(v2)
        val r = v1.getCrossProduct(v2)
            .add(v2.multiply(this.w))
            .add(v1.multiply(q.w))
        return Quaternion(w, r).normalize()
    }

    /**
     * 사원수 곱을 수행합니다. 단, this = q * this 형태로 진행됩니다.
     */
    operator fun timesAssign(q: Quaternion) {
        val v1 = q.r.clone()
        val v2 = r.clone()
        val w = w * q.w - v1.dot(v2)
        val r = v1.getCrossProduct(v2)
            .add(v2.multiply(q.w))
            .add(v1.multiply(this.w))
        this.w = w
        this.r.set(r)
        normalize()
    }

    operator fun unaryMinus(): Quaternion {
        return Quaternion(-w, -x, -y, -z)
    }

    /**
     * 회전 행렬을 구합니다. (세 기저 축 벡터를 회전)
     */
    fun toRotationMatrix(): Matrix3x3 {
        val vectors = standardBasisVectors
        vectors[0] *= this
        vectors[1] *= this
        vectors[2] *= this
        return Matrix3x3(vectors)
    }
    val back; get() = rotateVector(VectorUtil.BACK)
    val forward; get() = back.multiply(-1)
    val right; get() = rotateVector(VectorUtil.RIGHT)
    val left; get() = right.multiply(-1)
    val up; get() = rotateVector(VectorUtil.UP)
    val down; get() = up.multiply(-1)
    /**
     * 오일러 각으로 변환합니다.
     */
    fun toEulerAngle(): EulerAngle {
        val ySquared = y * y
        val srcy = 2 * (w * z + x * y)
        val crcy = 1.0 - 2 * (z * z + ySquared)
        val roll = atan2(srcy, crcy)

        val yawTest = w * y - x * z
        val yaw =
            if(yawTest < -asinThreshold) -90.0
            else if(yawTest > asinThreshold) 90.0
            else asin(2 * yawTest)

        val spcy = 2 * (w * x + y * z)
        val cpcy = 1.0 - 2 * (x * x + ySquared)
        val pitch = atan2(spcy, cpcy)

        // 마인크래프트의 회전은 pitch는 반시계방향, yaw와 roll은 시계방향 사용 ㅋㅋ
        return EulerAngle(pitch, -yaw, -roll)
    }

    fun lengthSquared() = x * x + y * y + z * z + w * w
    fun length() = sqrt(lengthSquared())
    fun normalize(): Quaternion {
        val lengthSquared = lengthSquared()
        // 0에 근접하는 경우 identity로 설정
        if(equalsInTolerance(lengthSquared, 0.0)) {
            w = 1.0
            r.set(0.0, 0.0, 0.0)
            return this
        }
        val scale = 1.0 / sqrt(lengthSquared)
        w *= scale
        r.multiply(scale)
        return this
    }
    fun dot(q: Quaternion) = x * q.x + y * q.y + z * q.z + w * q.w
    fun angle(q: Quaternion) = acos(dot(q))

    companion object {
        val identity; get() = Quaternion(1.0, 0.0, 0.0, 0.0)
        private val nextIndex = arrayOf(1, 2, 0)
        private const val asinThreshold = 0.4999999995

        operator fun Vector.timesAssign(q: Quaternion) {
            q.rotateVector(this)
        }

        @JvmStatic
        fun lookRotation(forward: Vector, up: Vector = VectorUtil.UP) = Quaternion(forward, up)

        fun EulerAngle.toQuaternion() = Quaternion(this)

        /**
         * 구면 보간을 진행합니다.
         */
        @JvmStatic
        fun slerp(quaternion1: Quaternion, quaternion2: Quaternion, t: Double): Quaternion {
            val dotTest = quaternion1.dot(quaternion2)
            val dot: Double
            // 내적값 음수면 최단거리 사용하도록 방향 전환
            val q1 = if(dotTest < 0.0) {
                dot = -dotTest
                -quaternion1
            }else{
                dot = dotTest
                quaternion1
            }
            val q2 = quaternion2

            val alpha: Double
            val beta: Double
            // 사잇각이 작으면 선형보간 수행
            if(dot > 0.9995f) {
                alpha = 1.0 - t
                beta = t
            }else{
                val theta = acos(dot)
                val invSin = 1.0 / sin(theta)
                alpha = sin((1.0 - t) * theta) * invSin
                beta = sin(t * theta) * invSin
            }

            return Quaternion(
                alpha * q1.w + beta * q2.w,
                alpha * q1.x + beta * q2.x,
                alpha * q1.y + beta * q2.y,
                alpha * q1.z + beta * q2.z,
            )
        }

    }

    override fun toString(): String {
        return "(w=${w}, x=${x}, y=${y}, z=${z})"
    }

    fun copy() = Quaternion(this)
}