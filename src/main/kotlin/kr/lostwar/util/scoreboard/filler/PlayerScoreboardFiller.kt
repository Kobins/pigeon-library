package kr.lostwar.util.scoreboard.filler

import kr.lostwar.util.scoreboard.PlayerScoreboardController
import kr.lostwar.util.scoreboard.ScoreboardController
import net.kyori.adventure.text.Component

abstract class PlayerScoreboardFiller(val controller: PlayerScoreboardController) : ScoreboardController<Component> {

}