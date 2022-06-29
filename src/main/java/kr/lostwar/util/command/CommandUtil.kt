package kr.lostwar.util.command

import org.bukkit.Bukkit
import org.bukkit.command.Command

object CommandUtil {
    fun Command.register(fallback: String) {
        Bukkit.getCommandMap().register(fallback, this)
    }


    fun Iterable<String>.findStartsWithOrContains(name: String, ignoreCase: Boolean = true): List<String> {
        return filter { it.startsWith(name, ignoreCase) }.takeIf { it.isNotEmpty() }
            ?: filter { it.contains(name, ignoreCase) }
    }

}


