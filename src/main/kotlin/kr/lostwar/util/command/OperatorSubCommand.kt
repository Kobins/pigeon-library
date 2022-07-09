package kr.lostwar.util.command

import org.bukkit.command.CommandSender

abstract class OperatorSubCommand(name: String) : SubCommand(name) {
    override fun isSuggestible(sender: CommandSender) = sender.isOp
    override fun isExecutable(sender: CommandSender) = sender.isOp
}