package kr.lostwar.util.ui.text

import kr.lostwar.util.ui.text.StringUtil.colored
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun console(message: String) = consoleRaw(message.colored())
fun consoleWarn(message: String) = console("&cWARN: $message")
fun consoleRaw(message: String) = Bukkit.getConsoleSender().sendMessage(message)
fun consoleRaw(component: Component) = Bukkit.getConsoleSender().sendMessage(component)
fun CommandSender.colorMessage(message: String) = sendMessage(message.colored())
fun CommandSender.colorMessage(vararg message: String) = message.forEach{ sendMessage(it.colored() )}
fun CommandSender.errorMessage(message: String) = sendMessage("&c$message".colored())
fun CommandSender.errorMessage(vararg message: String) = message.forEach{ errorMessage(it)}

@Suppress("DEPRECATION")
fun Player.colorActionBar(message: String) = sendActionBar('&', message.colored())
@Suppress("DEPRECATION")
fun Player.colorTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = sendTitle(title.colored(), subtitle.colored(), fadeIn, stay, fadeOut)