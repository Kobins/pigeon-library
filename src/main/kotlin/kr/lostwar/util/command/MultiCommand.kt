package kr.lostwar.util.command

import kr.lostwar.util.ui.text.errorMessage
import org.bukkit.command.CommandSender

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