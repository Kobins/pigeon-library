@file:Suppress("DEPRECATION")

package kr.lostwar.util.ui.text

import net.md_5.bungee.api.ChatColor
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

object StringUtil {

    private val HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}")
    @Suppress("DEPRECATION")
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
        @Suppress("DEPRECATION")
        return ChatColor.translateAlternateColorCodes('&', msg)
    }
    fun String.uncolored(): String = ChatColor.stripColor(this)!!

    fun Iterable<String>.mapColored(): List<String>{
        return map { it.colored() }
    }


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
            return sumOf { it.unifontWidth }
        }

    fun String.getSize(sizer: (Char) -> Int) = sumOf(sizer)

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


}
