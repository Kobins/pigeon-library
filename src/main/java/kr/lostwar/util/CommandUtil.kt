package kr.lostwar.util

import kr.lostwar.util.text.errorMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun Command.register(fallback: String) {
    Bukkit.getCommandMap().register(fallback, this)
}

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
}

abstract class MultiCommand(
    name: String,
    fallback: String,
    description: String = "",
    usageMessage: String = "/$name",
    aliases: List<String> = ArrayList(),
) : SimpleCommand(name, fallback, description, usageMessage, aliases) {
    abstract val subCommands: List<SubCommand>
    val subCommandsByName by lazy { subCommands.associateBy { it.name } }
    open fun isExecutable(sender: CommandSender): Boolean = true
    open fun isSuggestible(sender: CommandSender): Boolean = true

    open fun usage(sender: CommandSender, label: String, args: Array<String>) {
        sender.errorMessage("Usage: /$label <${subCommands.joinToString("/") { it.name }}> <...>")
    }

    override fun onExecute(sender: CommandSender, label: String, args: Array<String>) {
        if(!isExecutable(sender)){
            return
        }
        if(args.isEmpty()){
            usage(sender, label, args)
            return
        }
        val subCommandString = args[0]
        val subCommand = subCommandsByName[subCommandString] ?: run {
            sender.errorMessage("invalid command &e$subCommandString")
            return
        }
        val subCommandExecuteData = SubCommandExecuteData(sender, this, label, args)
        with(subCommand) { subCommandExecuteData.execute() }
        return
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<String>): List<String>? {
        if(!isSuggestible(sender)){
            return null
        }
        if(args.isEmpty()){
            return null
        }
        val subCommand = args[0]
        return when(args.size) {
            1 -> subCommands
                .filter { it.isSuggestible(sender) }
                .map { it.name }
                .filter { it.startsWith(subCommand, ignoreCase = true) || it.contains(subCommand, ignoreCase = true) }
            else -> {
                val subCommandExecuteData = SubCommandExecuteData(sender, this, label, args)
                subCommands
                    .filter { it.isCommand(subCommand) }
                    .flatMap { with(it) { subCommandExecuteData.complete() ?: emptyList() } }
            }
        }

    }
}

abstract class SubCommand(
    val name: String,
){
    open fun isExecutable(sender: CommandSender): Boolean = true
    open fun isSuggestible(sender: CommandSender): Boolean = true
    abstract fun SubCommandExecuteData.execute()
    open fun SubCommandExecuteData.complete(): List<String>? = emptyList()

    fun isCommand(string: String) = string.equals(name, true)
    open fun getDescription(label: String) = "/$label $name : No Description"
}

abstract class OperatorSubCommand(english: String) : SubCommand(english) {
    override fun isSuggestible(sender: CommandSender) = sender.isOp
    override fun isExecutable(sender: CommandSender) = sender.isOp
}


fun Iterable<String>.findStartsWithOrContains(name: String, ignoreCase: Boolean = true): List<String> {
    return filter { it.startsWith(name, ignoreCase) }.takeIf { it.isNotEmpty() }
        ?: filter { it.contains(name, ignoreCase) }
}

data class SubCommandExecuteData(
    val sender: CommandSender,
    val command: Command,
    val label: String,
    val args: Array<String>,
    val index: Int = 0
) {
    val subCmd = args[index]
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubCommandExecuteData) return false

        if (sender != other.sender) return false
        if (command != other.command) return false
        if (label != other.label) return false
        if (!args.contentEquals(other.args)) return false
        if (index != other.index) return false
        if (subCmd != other.subCmd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + args.contentHashCode()
        result = 31 * result + index
        result = 31 * result + subCmd.hashCode()
        return result
    }
}