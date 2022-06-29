package kr.lostwar.util.scoreboard

interface ScoreboardController<T>{
    fun setContent(content: List<T>)
    fun clearContent() = setContent(emptyList())
}