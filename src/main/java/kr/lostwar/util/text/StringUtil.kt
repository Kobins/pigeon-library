package kr.lostwar.util.text

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


private val HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}")
val String.hexColored
    get() = net.md_5.bungee.api.ChatColor.of(this).toString()
fun String.colored(): String {
    var msg = this
    if(contains('#')){
        var match = HEX_PATTERN.matcher(this)
        while(match.find()){
            val color = substring(match.start(), match.end());
            msg = msg.replace(color, color.hexColored)
            match = HEX_PATTERN.matcher(msg)
        }
    }
    return ChatColor.translateAlternateColorCodes('&', msg)
}
fun String.uncolored(): String = ChatColor.stripColor(this)!!
fun console(message: String) = consoleRaw(message.colored())
fun consoleWarn(message: String) = console("&cWARN: $message")
fun consoleRaw(message: String) = Bukkit.getConsoleSender().sendMessage(message)
fun consoleRaw(component: Component) = Bukkit.getConsoleSender().sendMessage(component)
fun CommandSender.colorMessage(message: String) = sendMessage(message.colored())
fun CommandSender.colorMessage(vararg message: String) = message.forEach{ sendMessage(it.colored() )}
fun CommandSender.errorMessage(message: String) = sendMessage("&c$message".colored())
fun CommandSender.errorMessage(vararg message: String) = message.forEach{ errorMessage(it)}

fun Player.colorActionBar(message: String) = sendActionBar('&', message.colored())
fun Player.colorTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = sendTitle(title.colored(), subtitle.colored(), fadeIn, stay, fadeOut)

private val formatter = DecimalFormat("###,###")
fun formatNumber(number: Int) = formatter.format(number)
fun formatNumber(number: Long) = formatter.format(number)
fun formatNumber(number: Double) = formatter.format(number)

val Char.unifontWidth: Int
    get() {
        return when (this) {
            '\'', '!', '|' -> 1
            '.', ',', ':', ';', '`', '(', ')', '{', '}', '[', ']' -> 2
            '\"', '<', '>' -> 3
            ' ', '@', '#', '$', '%', '^', '&', '*', '\\', '/', '?',
            in 'a'..'z', in 'A'..'Z', in '0'..'9' -> 4
            in '가'..'힣' -> 8
            else -> 4
        }
    }
val String.unifontWidth: Int
    get() {
        return sumBy { it.unifontWidth }
    }

fun String.getSize(sizer: (Char) -> Int) = sumBy(sizer)

fun String.chunkedWith(divider: String = " ", newLiner: String = "\n", limitWidth: Int = 280, sizer: (String) -> Int = { it.unifontWidth }): List<String> {
    val dividerWidth = sizer(divider)
    var widthSum = 0
    val split = split(divider)
    val list = ArrayList<String>(sizer(this) / limitWidth)
    var joiner = StringJoiner(divider)
    for(x in split){
        val width = dividerWidth + sizer(x)
        if(widthSum + width > limitWidth) {
            list.add(joiner.toString())
            widthSum = width
            joiner = StringJoiner(divider).add(x.removePrefix(newLiner))
        }else{
            widthSum += width
            joiner.add(x)
        }
    }
    if(joiner.length() > 0){
        list.add(joiner.toString())
    }
    return list
}

val Long.millisecondsToSeconds: String; get() = "%.1f".format((this / 100) / 10.0)

enum class Backspaces(val mask: Int, val char: Char){
    BACKSPACE_1   (0b00000000001, '\uF801'),
    BACKSPACE_2   (0b00000000010, '\uF802'),
    BACKSPACE_4   (0b00000000100, '\uF804'),
    BACKSPACE_8   (0b00000001000, '\uF808'),
    BACKSPACE_16  (0b00000010000, '\uF809'),
    BACKSPACE_32  (0b00000100000, '\uF80A'),
    BACKSPACE_64  (0b00001000000, '\uF80B'),
    BACKSPACE_128 (0b00010000000, '\uF80C'),
    BACKSPACE_256 (0b00100000000, '\uF80D'),
    BACKSPACE_512 (0b01000000000, '\uF80E'),
    BACKSPACE_1024(0b10000000000, '\uF80F');

    override fun toString() = char.toString()

    companion object {
        @JvmStatic
        operator fun get(index: Int) = backspace[index]
        @JvmStatic
        private val backspace = (0..1024).toList().map {
            val builder = StringBuilder()
            for(space in values()){
                val masked = (it and space.mask)
                if(masked != 0){
                    builder.append(space.char)
                }
            }
            builder.toString()
        }
    }
}

enum class Spaces(val mask: Int, val char: Char){
    SPACE_1   (0b00000000001, '\uF821'),
    SPACE_2   (0b00000000010, '\uF822'),
    SPACE_4   (0b00000000100, '\uF824'),
    SPACE_8   (0b00000001000, '\uF828'),
    SPACE_16  (0b00000010000, '\uF829'),
    SPACE_32  (0b00000100000, '\uF82A'),
    SPACE_64  (0b00001000000, '\uF82B'),
    SPACE_128 (0b00010000000, '\uF82C'),
    SPACE_256 (0b00100000000, '\uF82D'),
    SPACE_512 (0b01000000000, '\uF82E'),
    SPACE_1024(0b10000000000, '\uF82F');

    override fun toString() = char.toString()

    companion object {
        @JvmStatic
        operator fun get(index: Int) = space[index]
        @JvmStatic
        private val space = (0..1024).toList().map {
            val builder = StringBuilder()
            for(space in values()){
                val masked = (it and space.mask)
                if(masked != 0){
                    builder.append(space.char)
                }
            }
            builder.toString()
        }
    }
}