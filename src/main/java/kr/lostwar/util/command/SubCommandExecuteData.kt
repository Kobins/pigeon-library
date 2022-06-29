package kr.lostwar.util.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

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