package kr.lostwar.util.command

import kr.lostwar.util.command.CommandUtil.register
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

abstract class SimpleCommand(
    name: String,
    val fallback: String,
    description: String = "",
    usageMessage: String = "/$name",
    aliases: List<String> = ArrayList(),
) : Command(name, description, usageMessage, aliases) {
    final override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        onExecute(sender, label, args)
        return true
    }
    abstract fun onExecute(sender: CommandSender, label: String, args: Array<String>)

    final override fun tabComplete(sender: CommandSender, label: String, args: Array<String>): List<String> {
        return onTabComplete(sender, label, args) ?: emptyList()
    }
    open fun onTabComplete(sender: CommandSender, label: String, args: Array<String>): List<String>? {
        return null
    }
    fun register() {
        register(fallback)
    }
}