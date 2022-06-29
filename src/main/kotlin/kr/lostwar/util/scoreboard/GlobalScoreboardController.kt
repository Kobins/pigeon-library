package kr.lostwar.util.scoreboard

import kr.lostwar.util.scoreboard.PlayerScoreboardController.Companion.scoreboardController
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object GlobalScoreboardController : ScoreboardController<Component> {
    override fun setContent(content: List<Component>) {
        Bukkit.getOnlinePlayers().forEach { it.scoreboardController.setContent(content) }
    }
    fun setContentEach(mapNotNull: (Player) -> List<Component>?) = Bukkit.getOnlinePlayers().forEach {
        val contents = mapNotNull(it) ?: emptyList()
        it.scoreboardController.setContent(contents)
    }
    fun setTitle(title: Component) = Bukkit.getOnlinePlayers().forEach { it.scoreboardController.title = title }
}