package kr.lostwar.util.math

import kotlin.random.Random

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
fun ClosedFloatingPointRange<Double>.lerp(t: Double) = lerp(start, endInclusive, (t.clamp(0.0..1.0)))
fun ClosedFloatingPointRange<Double>.center() = lerp(0.5)
fun ClosedFloatingPointRange<Double>.random() = lerp(Random.nextDouble())