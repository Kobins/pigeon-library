package kr.lostwar.util

import kotlin.math.max
import kotlin.math.min

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