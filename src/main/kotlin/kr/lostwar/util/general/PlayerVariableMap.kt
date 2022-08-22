package kr.lostwar.util.general

import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

open class PlayerVariableMap<T>(
    protected val map: HashMap<UUID, T> = HashMap()
) : MutableMap<UUID, T> by map {

    open operator fun get(player: Player): T? = map[player.uniqueId]
    open operator fun set(player: Player, value: T?) {
        if(value == null) remove(player)
        else map[player.uniqueId] = value
    }

    open fun remove(player: Player) = map.remove(player.uniqueId)

}