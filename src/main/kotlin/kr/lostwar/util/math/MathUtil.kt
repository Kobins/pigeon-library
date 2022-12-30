package kr.lostwar.util.math

import kotlin.math.abs
import kotlin.random.Random

const val smallNumber = 1.0e-8
const val smallNumberInFloat = 1.0e-8f
fun equalsInTolerance(a: Double, b: Double, tolerance: Double = smallNumber): Boolean {
    return abs(a - b) <= tolerance
}
fun equalsInTolerance(a: Float, b: Float, tolerance: Float = smallNumberInFloat): Boolean {
    return abs(a - b) <= tolerance
}

fun lerp(start: Double, end: Double, percentage: Double): Double{
    return start + (end - start) * percentage
}
fun lerp(start: Float, end: Float, percentage: Float): Float{
    return start + (end - start) * percentage
}

fun invLerp(start: Double, end: Double, value: Double) = invLerpUnclamped(start, end, value.clamp(start..end))
fun invLerp(start: Int, end: Int, value: Int) = invLerpUnclamped(start, end, value.clamp(start..end))
fun invLerpUnclamped(start: Double, end: Double, value: Double): Double{
    return (value - start) / (end - start)
}
fun invLerpUnclamped(start: Int, end: Int, value: Int): Double{
    return (value - start).toDouble() / (end - start)
}

fun <T : Comparable<T>> T.clamp(range: ClosedRange<T>) = maxOf(range.start, minOf(range.endInclusive, this))

fun Double.toRadians() = Math.toRadians(this)
fun Double.toDegrees() = Math.toDegrees(this)

val IntRange.size: Int
    get() = endInclusive - start + 1

private val doubleRange01 = 0.0..1.0
private val floatRange01 = 0f..1f
fun ClosedFloatingPointRange<Double>.lerp(t: Double) = lerp(start, endInclusive, (t.clamp(doubleRange01)))
fun ClosedFloatingPointRange<Double>.center() = lerp(0.5)
fun ClosedFloatingPointRange<Double>.random() = lerp(Random.nextDouble())

fun ClosedFloatingPointRange<Float>.lerp(t: Float) = lerp(start, endInclusive, (t.clamp(floatRange01)))
fun ClosedFloatingPointRange<Float>.center() = lerp(0.5f)
fun ClosedFloatingPointRange<Float>.random() = lerp(Random.nextFloat())