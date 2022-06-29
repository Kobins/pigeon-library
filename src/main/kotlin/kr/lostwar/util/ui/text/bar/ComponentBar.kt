package kr.lostwar.util.ui.text.bar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.util.*

open class ComponentBar @JvmOverloads constructor(
    amount: Int = 100,
    private val leftSymbol: String = "|",
    private val rightSymbol: String = "|",
    private val join: String = "",
    private val leftColor: TextColor? = NamedTextColor.WHITE,
    private val rightColor: TextColor? = NamedTextColor.DARK_GRAY,
    private val leftBlock: Component.() -> Component = { this },
    private val rightBlock: Component.() -> Component = { this },
    private val removeRight: Boolean = false
) : CachedBar<Component>(amount) {

    constructor(
        amount: Int = 100,
        leftSymbol: String = "|",
        join: String = "",
        leftColor: TextColor? = NamedTextColor.WHITE,
        rightColor: TextColor? = NamedTextColor.DARK_GRAY,
        leftBlock: Component.() -> Component = { this },
        rightBlock: Component.() -> Component = { this },
        removeRight: Boolean = false
    ) : this(amount, leftSymbol, leftSymbol, join, leftColor, rightColor, leftBlock, rightBlock, removeRight)

    override fun getBar(index: Int): Component {
        val left = StringJoiner(join)
        for(i in 0 until index){
            left.add(leftSymbol)
        }
        val right = StringJoiner(join)
        if(!removeRight){
            for(i in index until amount){
                right.add(rightSymbol)
            }
        }
        return text()
            .append(text(left.toString()).color(leftColor).leftBlock())
            .apply {
                if(!removeRight)
                    it.append(text(join))
                        .append(text(right.toString()).color(rightColor).rightBlock())

            }.build()
    }

}