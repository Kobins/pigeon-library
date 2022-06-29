package kr.lostwar.util.scoreboard

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import java.util.*

class PlayerScoreboardController(
    val player: Player,
) : ScoreboardController<Component> {
    companion object {
        private val playerScoreboardMap = HashMap<UUID, PlayerScoreboardController>()
        @JvmStatic
        val Player.scoreboardController: PlayerScoreboardController
            get() = playerScoreboardMap.computeIfAbsent(uniqueId) { PlayerScoreboardController(this) }
                .takeIf { it.player == player }
                ?: PlayerScoreboardController(this).also { playerScoreboardMap[uniqueId] = it }
    }

    val scoreboard by lazy { Bukkit.getScoreboardManager().newScoreboard }
    val objective by lazy {
        scoreboard.registerNewObjective(" ", "dummy", Component.empty()).apply {
            displaySlot = DisplaySlot.SIDEBAR
        }
    }
    var title: Component
        get() = objective.displayName()
        set(value) { if(value != objective.displayName()) objective.displayName(value) }

    private var lastContent: List<Component> = emptyList()

    init {
        player.scoreboard = scoreboard
    }
    private val colors = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray()
    private val teamStrings = colors.map { "§${it}§r" }
    private val Int.teamString: String get() = teamStrings[this % teamStrings.size]
    private fun Int.getScore(size: Int) = size - this - 1
    private fun Int.updateScore(size: Int): Int {
        val score = getScore(size)
        objective.getScore(teamString).score = score
        return score
    }
    override fun setContent(content: List<Component>) {
        if (lastContent == content)
            return
//        console("content changed: ${lastContent.size} -> ${content.size}")
        val reduced = content.size < lastContent.size
        if(reduced) {
//            console("- reduced, clear ${content.size}(${content.size.getScore(lastContent.size)}) until ${lastContent.size}(${lastContent.size.getScore(lastContent.size)})")
            (content.size..lastContent.size).forEach { index ->
                scoreboard.resetScores(index.teamString)
//                console("  * removed $index")
            }
        }
        for ((index, line) in content.withIndex()) {
            val teamString = index.teamString
            val team = scoreboard.getTeam(teamString) ?: scoreboard.registerNewTeam(teamString)
            if(!team.hasEntry(teamString)) {
                team.addEntry(teamString)
            }
            if(objective.getScore(teamString).score != index.getScore(content.size)
                || index >= lastContent.size
                || line != lastContent[index]
            ){
//                consoleRaw(text("[${index}, ${index.getScore(content.size)}] ", NamedTextColor.GOLD).append(line))
                team.prefix(line)
                index.updateScore(content.size)
            }
        }

        lastContent = content
    }
}