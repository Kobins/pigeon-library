package kr.lostwar.util.text

import kr.lostwar.util.clamp
import kotlin.math.max
import kotlin.math.min

abstract class CachedBar<T>(protected val amount: Int) {
    protected abstract fun getBar(index: Int): T
    private val range = 0..amount
    private val cachedMap by lazy { range.toList().map { getBar(it) } }
    operator fun get(index: Int): T {
        val clamped = index.clamp(range)
        return cachedMap[clamped]
    }
    operator fun get(percent: Double): T {
        return get((percent * amount).toInt())
    }
}