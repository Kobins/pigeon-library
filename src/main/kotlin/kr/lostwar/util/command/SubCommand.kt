package kr.lostwar.util.command

import org.bukkit.command.CommandSender

abstract class SubCommand(
    val name: String,
){
    open fun isExecutable(sender: CommandSender): Boolean = true
    open fun isSuggestible(sender: CommandSender): Boolean = isExecutable(sender)
    abstract fun SubCommandExecuteData.execute()
    open fun SubCommandExecuteData.complete(): List<String>? = emptyList()

    fun isCommand(string: String) = string.equals(name, true)
    open fun getDescription(label: String) = "/$label $name : No Description"
}