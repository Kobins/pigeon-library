package kr.lostwar.util.scoreboard.filler

import kr.lostwar.util.scoreboard.PlayerScoreboardController
import kr.lostwar.util.ui.text.Backspaces
import kr.lostwar.util.ui.text.Spaces
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

class NSFScoreboardFiller(controller: PlayerScoreboardController) : PlayerScoreboardFiller(controller) {

    companion object {

        private val teamStrings = (0..30).map { "${Backspaces.BACKSPACE_32768.char}$it" }
        private val Int.teamString: String get() = teamStrings[this % teamStrings.size]
        private fun Int.getScore(size: Int) = size - this - 1
    }

    init {
        controller.title = text(Backspaces.BACKSPACE_4.char.toString())
    }

    private fun Int.updateScore(size: Int): Int {
        val score = getScore(size)
        controller.objective.getScore(teamString).score = score
        return score
    }

    private var lastContent: List<Component> = emptyList()
    override fun setContent(content: List<Component>) {
        if (lastContent == content)
            return
//        console("content changed: ${lastContent.size} -> ${content.size}")
        val reduced = content.size < lastContent.size
        if(reduced) {
//            console("- reduced, clear ${content.size}(${content.size.getScore(lastContent.size)}) until ${lastContent.size}(${lastContent.size.getScore(lastContent.size)})")
            (content.size..lastContent.size).forEach { index ->
                controller.scoreboard.resetScores(index.teamString)
//                console("  * removed $index")
            }
        }
        for ((index, line) in content.withIndex()) {
            val teamString = index.teamString
            val team = controller.scoreboard.getTeam(teamString) ?: controller.scoreboard.registerNewTeam(teamString)
            if(!team.hasEntry(teamString)) {
                team.addEntry(teamString)
            }
            if(controller.objective.getScore(teamString).score != index.getScore(content.size)
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