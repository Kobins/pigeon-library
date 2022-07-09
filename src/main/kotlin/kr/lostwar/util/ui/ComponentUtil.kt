@file:Suppress("DEPRECATION")

package kr.lostwar.util.ui

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.*
import net.kyori.adventure.text.TextComponent.Builder
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.util.Ticks
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
object ComponentUtil {


    @JvmStatic
    fun translateObject(key: String, vararg objects: Any)
        = translate(key, *objects.map{if(it is ComponentLike) it else Component.text(it.toString()) }.toTypedArray())
    @JvmStatic
    fun translate(key: String, vararg components: ComponentLike)
        = Component.translatable(key).args(*components).asComponent()

    fun Component.bold() = decorate(BOLD)
    fun Component.underlined() = decorate(UNDERLINED)
    fun Component.strikethrough() = decorate(STRIKETHROUGH)

    @JvmStatic fun Component.black() = color(NamedTextColor.BLACK).noitalic()
    @JvmStatic fun Component.darkBlue() = color(NamedTextColor.DARK_BLUE).noitalic()
    @JvmStatic fun Component.darkGreen() = color(NamedTextColor.DARK_GREEN).noitalic()
    @JvmStatic fun Component.darkAqua() = color(NamedTextColor.DARK_AQUA).noitalic()
    @JvmStatic fun Component.darkRed() = color(NamedTextColor.DARK_RED).noitalic()
    @JvmStatic fun Component.darkPurple() = color(NamedTextColor.DARK_PURPLE).noitalic()
    @JvmStatic fun Component.gold() = color(NamedTextColor.GOLD).noitalic()
    @JvmStatic fun Component.gray() = color(NamedTextColor.GRAY).noitalic()
    @JvmStatic fun Component.darkGray() = color(NamedTextColor.DARK_GRAY).noitalic()
    @JvmStatic fun Component.blue() = color(NamedTextColor.BLUE).noitalic()
    @JvmStatic fun Component.green() = color(NamedTextColor.GREEN).noitalic()
    @JvmStatic fun Component.aqua() = color(NamedTextColor.AQUA).noitalic()
    @JvmStatic fun Component.red() = color(NamedTextColor.RED).noitalic()
    @JvmStatic fun Component.lightPurple() = color(NamedTextColor.LIGHT_PURPLE).noitalic()
    @JvmStatic fun Component.yellow() = color(NamedTextColor.YELLOW).noitalic()
    @JvmStatic fun Component.white() = color(NamedTextColor.WHITE).noitalic()
    @JvmStatic fun Component.noitalic() = decoration(ITALIC, false)
    @JvmStatic fun Component.appendSpace() = append(Component.space())
//    @JvmStatic fun Component.font(key: Key) = style(style().font(key))



    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.black() = color(
        NamedTextColor.BLACK
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkBlue() = color(
        NamedTextColor.DARK_BLUE
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkGreen() = color(
        NamedTextColor.DARK_GREEN
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkAqua() = color(
        NamedTextColor.DARK_AQUA
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkRed() = color(
        NamedTextColor.DARK_RED
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkPurple() = color(
        NamedTextColor.DARK_PURPLE
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.gold() = color(
        NamedTextColor.GOLD
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.gray() = color(
        NamedTextColor.GRAY
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.darkGray() = color(
        NamedTextColor.DARK_GRAY
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.blue() = color(
        NamedTextColor.BLUE
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.green() = color(
        NamedTextColor.GREEN
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.aqua() = color(
        NamedTextColor.AQUA
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.red() = color(
        NamedTextColor.RED
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.lightPurple() = color(
        NamedTextColor.LIGHT_PURPLE
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.yellow() = color(
        NamedTextColor.YELLOW
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.white() = color(
        NamedTextColor.WHITE
    ).noitalic()
    @JvmStatic fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.noitalic() = decoration(
        ITALIC, false)
    @JvmStatic fun Builder.appendSpace() = append(Component.space())

    fun Component.appendText(string: String, block: TextComponent.() -> Component = {
        this
    }): Component {
        val component = Component.text(string).block()
        return append(component)
    }
    fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.appendText(
        string: String, block: TextComponent.() -> Component = { this }
    ): B {
        return append(Component.text(string).block())
    }
    fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.appendSpace() = append(Component.space())

    fun org.bukkit.ChatColor.asAdventure(): NamedTextColor = when(this){
        org.bukkit.ChatColor.BLACK -> NamedTextColor.BLACK
        org.bukkit.ChatColor.DARK_BLUE -> NamedTextColor.DARK_BLUE
        org.bukkit.ChatColor.DARK_GREEN -> NamedTextColor.DARK_GREEN
        org.bukkit.ChatColor.DARK_AQUA -> NamedTextColor.DARK_AQUA
        org.bukkit.ChatColor.DARK_RED -> NamedTextColor.DARK_RED
        org.bukkit.ChatColor.DARK_PURPLE -> NamedTextColor.DARK_PURPLE
        org.bukkit.ChatColor.GOLD -> NamedTextColor.GOLD
        org.bukkit.ChatColor.GRAY -> NamedTextColor.GRAY
        org.bukkit.ChatColor.DARK_GRAY -> NamedTextColor.DARK_GRAY
        org.bukkit.ChatColor.BLUE -> NamedTextColor.BLUE
        org.bukkit.ChatColor.GREEN -> NamedTextColor.GREEN
        org.bukkit.ChatColor.AQUA -> NamedTextColor.AQUA
        org.bukkit.ChatColor.RED -> NamedTextColor.RED
        org.bukkit.ChatColor.LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE
        org.bukkit.ChatColor.YELLOW -> NamedTextColor.YELLOW
        org.bukkit.ChatColor.WHITE -> NamedTextColor.WHITE
//        ChatColor.BOLD -> BOLD
//        ChatColor.UNDERLINE -> UNDERLINED
//        ChatColor.STRIKETHROUGH -> STRIKETHROUGH
//        ChatColor.ITALIC -> ITALIC
        else -> NamedTextColor.WHITE
    }
    fun ChatColor.asAdventure(): NamedTextColor = when(this){
        ChatColor.BLACK -> NamedTextColor.BLACK
        ChatColor.DARK_BLUE -> NamedTextColor.DARK_BLUE
        ChatColor.DARK_GREEN -> NamedTextColor.DARK_GREEN
        ChatColor.DARK_AQUA -> NamedTextColor.DARK_AQUA
        ChatColor.DARK_RED -> NamedTextColor.DARK_RED
        ChatColor.DARK_PURPLE -> NamedTextColor.DARK_PURPLE
        ChatColor.GOLD -> NamedTextColor.GOLD
        ChatColor.GRAY -> NamedTextColor.GRAY
        ChatColor.DARK_GRAY -> NamedTextColor.DARK_GRAY
        ChatColor.BLUE -> NamedTextColor.BLUE
        ChatColor.GREEN -> NamedTextColor.GREEN
        ChatColor.AQUA -> NamedTextColor.AQUA
        ChatColor.RED -> NamedTextColor.RED
        ChatColor.LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE
        ChatColor.YELLOW -> NamedTextColor.YELLOW
        ChatColor.WHITE -> NamedTextColor.WHITE
//        ChatColor.BOLD -> BOLD
//        ChatColor.UNDERLINE -> UNDERLINED
//        ChatColor.STRIKETHROUGH -> STRIKETHROUGH
//        ChatColor.ITALIC -> ITALIC
        else -> NamedTextColor.WHITE
    }

    @JvmStatic
    fun Component.chatColor(color: org.bukkit.ChatColor) = when(color){
        org.bukkit.ChatColor.BLACK -> color(NamedTextColor.BLACK)
        org.bukkit.ChatColor.DARK_BLUE -> color(NamedTextColor.DARK_BLUE)
        org.bukkit.ChatColor.DARK_GREEN -> color(NamedTextColor.DARK_GREEN)
        org.bukkit.ChatColor.DARK_AQUA -> color(NamedTextColor.DARK_AQUA)
        org.bukkit.ChatColor.DARK_RED -> color(NamedTextColor.DARK_RED)
        org.bukkit.ChatColor.DARK_PURPLE -> color(NamedTextColor.DARK_PURPLE)
        org.bukkit.ChatColor.GOLD -> color(NamedTextColor.GOLD)
        org.bukkit.ChatColor.GRAY -> color(NamedTextColor.GRAY)
        org.bukkit.ChatColor.DARK_GRAY -> color(NamedTextColor.DARK_GRAY)
        org.bukkit.ChatColor.BLUE -> color(NamedTextColor.BLUE)
        org.bukkit.ChatColor.GREEN -> color(NamedTextColor.GREEN)
        org.bukkit.ChatColor.AQUA -> color(NamedTextColor.AQUA)
        org.bukkit.ChatColor.RED -> color(NamedTextColor.RED)
        org.bukkit.ChatColor.LIGHT_PURPLE -> color(NamedTextColor.LIGHT_PURPLE)
        org.bukkit.ChatColor.YELLOW -> color(NamedTextColor.YELLOW)
        org.bukkit.ChatColor.WHITE -> color(NamedTextColor.WHITE)
        org.bukkit.ChatColor.BOLD -> decorate(BOLD)
        org.bukkit.ChatColor.UNDERLINE -> decorate(UNDERLINED)
        org.bukkit.ChatColor.STRIKETHROUGH -> decorate(STRIKETHROUGH)
        org.bukkit.ChatColor.ITALIC -> decorate(ITALIC)
        else -> this
    }

    @JvmStatic
    fun Component.chatColor(color: ChatColor) = when(color){
        ChatColor.BLACK -> color(NamedTextColor.BLACK)
        ChatColor.DARK_BLUE -> color(NamedTextColor.DARK_BLUE)
        ChatColor.DARK_GREEN -> color(NamedTextColor.DARK_GREEN)
        ChatColor.DARK_AQUA -> color(NamedTextColor.DARK_AQUA)
        ChatColor.DARK_RED -> color(NamedTextColor.DARK_RED)
        ChatColor.DARK_PURPLE -> color(NamedTextColor.DARK_PURPLE)
        ChatColor.GOLD -> color(NamedTextColor.GOLD)
        ChatColor.GRAY -> color(NamedTextColor.GRAY)
        ChatColor.DARK_GRAY -> color(NamedTextColor.DARK_GRAY)
        ChatColor.BLUE -> color(NamedTextColor.BLUE)
        ChatColor.GREEN -> color(NamedTextColor.GREEN)
        ChatColor.AQUA -> color(NamedTextColor.AQUA)
        ChatColor.RED -> color(NamedTextColor.RED)
        ChatColor.LIGHT_PURPLE -> color(NamedTextColor.LIGHT_PURPLE)
        ChatColor.YELLOW -> color(NamedTextColor.YELLOW)
        ChatColor.WHITE -> color(NamedTextColor.WHITE)
        ChatColor.BOLD -> decorate(BOLD)
        ChatColor.UNDERLINE -> decorate(UNDERLINED)
        ChatColor.STRIKETHROUGH -> decorate(STRIKETHROUGH)
        ChatColor.ITALIC -> decorate(ITALIC)
        else -> color(TextColor.color(color.color.rgb))
    }
    @JvmStatic val String.asComponent
        get() = LegacyComponentSerializer.legacySection().deserialize(this).noitalic()

    @JvmStatic
    fun Component.toJSONString(): String {
        return GsonComponentSerializer.gson().serialize(this)
    }

    fun title(
        title: Component,
        subtitle: Component,
        fadeIn: Long,
        stay: Long,
        fadeOut: Long
    ) = Title.title(title, subtitle, Times.times(
        Ticks.duration(fadeIn),
        Ticks.duration(stay),
        Ticks.duration(fadeOut),
    ))



    operator fun Component.plus(other: ComponentLike) = append(other)

    fun Player.showTitle(
        title: Component,
        subtitle: Component,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) = showTitle(title(title, subtitle, fadeIn.toLong(), stay.toLong(), fadeOut.toLong()))

    fun Player.showTitle(
        title: ComponentLike,
        subtitle: ComponentLike,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) = showTitle(title(title.asComponent(), subtitle.asComponent(), fadeIn.toLong(), stay.toLong(), fadeOut.toLong()))


    fun Player.sendMessages(vararg components: ComponentLike) = components.forEach { sendMessage(it) }
}

