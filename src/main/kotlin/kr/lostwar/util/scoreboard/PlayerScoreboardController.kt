package kr.lostwar.util.scoreboard

import kr.lostwar.util.scoreboard.filler.GeneralScoreboardFiller
import kr.lostwar.util.scoreboard.filler.PlayerScoreboardFiller
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import java.util.*

class PlayerScoreboardController private constructor(
    val player: Player,
) : ScoreboardController<Component> {
    companion object {
        var scoreboardFiller: (PlayerScoreboardController) -> PlayerScoreboardFiller = { GeneralScoreboardFiller(it) }
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
        set(value) {
            if (value != objective.displayName()) objective.displayName(value)
        }

    init {
        player.scoreboard = scoreboard
    }

    private val filler = scoreboardFiller(this)
    override fun setContent(content: List<Component>) = filler.setContent(content)
}