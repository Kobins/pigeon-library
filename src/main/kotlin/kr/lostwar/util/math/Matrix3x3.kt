package kr.lostwar.util.math

import kr.lostwar.util.math.VectorUtil.set
import kr.lostwar.util.math.VectorUtil.standardBasisVectors
import org.bukkit.util.Vector

class Matrix3x3(
    val cols: Array<Vector> = standardBasisVectors
) {
    constructor(col0: Vector, col1: Vector, col2: Vector) : this(arrayOf(col0, col1, col2))

    operator fun get(index: Int) = cols[index]
    operator fun set(index: Int, value: Vector) { cols[index] = value }

    operator fun component1(): Vector = get(0)
    operator fun component2(): Vector = get(1)
    operator fun component3(): Vector = get(2)


    // Matrix3x3 * Vector
    operator fun times(v: Vector): Vector {
        val x = v.x * cols[0].x + v.y * cols[1].x + v.z * cols[2].x
        val y = v.x * cols[0].y + v.y * cols[1].y + v.z * cols[2].y
        val z = v.x * cols[0].z + v.y * cols[1].z + v.z * cols[2].z
        return Vector(x, y, z)
    }
    companion object {
        // Vector *= Matrix3x3 -> Vector = Matrix3x3 * Vector
        operator fun Vector.timesAssign(m: Matrix3x3) {
            val x = this.x * m.cols[0].x + this.y * m.cols[1].x + this.z * m.cols[2].x
            val y = this.x * m.cols[0].y + this.y * m.cols[1].y + this.z * m.cols[2].y
            val z = this.x * m.cols[0].z + this.y * m.cols[1].z + this.z * m.cols[2].z
            set(x, y, z)
        }
    }
}