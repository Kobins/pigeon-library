package kr.lostwar.util

import kr.lostwar.util.math.VectorUtil.div
import kr.lostwar.util.math.VectorUtil.minus
import kr.lostwar.util.math.VectorUtil.times
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object ColorUtil {

    private val colorRange = 0..255
    private infix fun Int.clamp(range: IntRange) = max(range.first, min(range.last, this))
    operator fun Color.plus(other: Color) = Color(
        (red + other.red) clamp colorRange,
        (green + other.green) clamp colorRange,
        (blue + other.blue) clamp colorRange,
        (alpha + other.alpha) clamp colorRange,
    )
    operator fun Color.minus(other: Color) = Color(
        (red - other.red) clamp colorRange,
        (green - other.green) clamp colorRange,
        (blue - other.blue) clamp colorRange,
        (alpha - other.alpha) clamp colorRange,
    )
    operator fun Color.times(scala: Double) = Color(
        (red * scala).toInt() clamp colorRange,
        (green * scala).toInt() clamp colorRange,
        (blue * scala).toInt() clamp colorRange,
        (alpha * scala).toInt() clamp colorRange,
    )

    operator fun TextColor.plus(other: Vector) = TextColor.color(
        (red() + other.x).toInt() clamp colorRange,
        (green() + other.y).toInt() clamp colorRange,
        (blue() + other.z).toInt() clamp colorRange,
    )
    operator fun TextColor.plus(other: TextColor) = TextColor.color(
        (red() + other.red()) clamp colorRange,
        (green() + other.green()) clamp colorRange,
        (blue() + other.blue()) clamp colorRange,
    )
    operator fun TextColor.minus(other: TextColor) = TextColor.color(
        (red() - other.red()) clamp colorRange,
        (green() - other.green()) clamp colorRange,
        (blue() - other.blue()) clamp colorRange,
    )
    operator fun TextColor.times(scala: Double) = TextColor.color(
        (red() * scala).toInt() clamp colorRange,
        (green() * scala).toInt() clamp colorRange,
        (blue() * scala).toInt() clamp colorRange,
    )
    operator fun TextColor.div(scala: Double) = TextColor.color(
        (red() / scala).toInt() clamp colorRange,
        (green() / scala).toInt() clamp colorRange,
        (blue() / scala).toInt() clamp colorRange,
    )

    val TextColor.asVector: Vector
        get() = Vector(red(), green(), blue())

    fun lerp(start: Color, end: Color, percentage: Double)
            = start + (end-start) * percentage

    fun lerp(t: Double, from: TextColor, to: TextColor): TextColor {
        val clampedT = min(1.0, max(0.0, t))
        val ar = from.red()
        val br = to.red()
        val ag = from.green()
        val bg = to.green()
        val ab = from.blue()
        val bb = to.blue()
        val result = TextColor.color(
            round(ar + clampedT * (br - ar)).toInt(),
            round(ag + clampedT * (bg - ag)).toInt(),
            round(ab + clampedT * (bb - ab)).toInt()
        )
//    if(result.value() == from.value()){
//        return to
//    }
        return result
    }

    val TextColor.length: Double
        get() = asVector.length()

    val TextColor.normalized: TextColor
        get() {
            val vector = asVector
            val length = vector.length()
            return (vector / length).asTextColor
        }

    fun moveTo(amount: Double, from: TextColor, to: TextColor): TextColor {
        val diff = (to.asVector - from.asVector)
        val distance = diff.length()
        if (distance <= amount) { //남은 거리가 amount보다 작을 경우
            return to
        }
        val move = diff.normalize() * amount
        return from + move

    }

    val Vector.asTextColor: TextColor
        get() = TextColor.color(x.toInt(), y.toInt(), z.toInt())

    fun String.parseBukkitColor(): org.bukkit.Color? {
        val raw = this.let { if(it.startsWith('#')) it.substring(1) else it }
        val namedTextColor = NamedTextColor.NAMES.value(raw.lowercase())
        if(namedTextColor != null){
            return namedTextColor.toBukkitColor()
        }
        if(raw.contains(',')) { // 255, 0, 255
            val split = raw.split(',').map { it.trim() }
            if (split.size == 3) {
                val r = split[0].toIntOrNull() ?: return null
                val g = split[1].toIntOrNull() ?: return null
                val b = split[2].toIntOrNull() ?: return null
                return org.bukkit.Color.fromRGB(r, g, b)
            } else return null
        }
        return if(raw.length == 6){ // FF00FF
            val hexParsed = raw.toIntOrNull(16)
            if(hexParsed != null)
                org.bukkit.Color.fromRGB(hexParsed)
            else null
        }else null
    }

    fun ConfigurationSection.getBukkitColor(dir: String): org.bukkit.Color? {
        if(isString(dir)) { // parse hex color
            return getString(dir)!!.parseBukkitColor()
        }
        if(isConfigurationSection(dir)){
            val section = getConfigurationSection(dir)!!
            val redInt = section.getInt("r", -1).takeIf { it != -1 }
            val greenInt = section.getInt("g", -1).takeIf { it != -1 }
            val blueInt = section.getInt("b", -1).takeIf { it != -1 }
            if(redInt != null && greenInt != null && blueInt != null){
                return org.bukkit.Color.fromRGB(redInt, greenInt, blueInt)
            }
            val redDouble = section.getDouble("r", -1.0).takeIf { it != -1.0 }
            val greenDouble = section.getDouble("g", -1.0).takeIf { it != -1.0 }
            val blueDouble = section.getDouble("b", -1.0).takeIf { it != -1.0 }
            if(redDouble != null && greenDouble != null && blueDouble != null){
                return org.bukkit.Color.fromRGB((redDouble * 255).toInt(), (greenDouble * 255).toInt(), (blueDouble * 255).toInt())
            }
            return null
        }
        return null
    }

    fun org.bukkit.Color.toTextColor() = TextColor.color(red, green, blue)

    fun TextColor.toBukkitColor(): org.bukkit.Color {
        return org.bukkit.Color.fromRGB(value())
    }
}