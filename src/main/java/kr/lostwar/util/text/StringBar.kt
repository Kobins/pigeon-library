package kr.lostwar.util.text

import java.util.*

open class StringBar(
    amount: Int = 100,
    val leftColor: String = "§f",
    val rightColor: String = "§e",
    val symbol: String = "|",
    val join: String = "",
) : CachedBar<String>(amount) {
    override fun getBar(index: Int): String {
        val left = StringJoiner(join, leftColor, rightColor+join)
        for(i in 0 until index){
            left.add(symbol)
        }
        val right = StringJoiner(join, rightColor, "")
        for(i in index until amount){
            right.add(symbol)
        }
        return left.toString()+right.toString()
    }
}