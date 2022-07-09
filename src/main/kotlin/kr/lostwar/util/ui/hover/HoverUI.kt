package kr.lostwar.util.ui.hover

import org.bukkit.entity.Player
import java.util.*

class HoverUI private constructor(
    val player: Player,
){
    private val sessionsMap = HashMap<String, HoverUISession>()

    operator fun get(key: String): HoverUISession{
        return sessionsMap.computeIfAbsent(key) { HoverUISession(player, it) }
    }

    fun remove(key: String){
        val session = sessionsMap[key] ?: return
        session.destroy()
        sessionsMap.remove(key)
    }

    fun destroy(){
        sessionsMap.forEach { (_, session) ->
            session.destroy()
        }
        sessionsMap.clear()
        playerMap.remove(player.uniqueId)
    }

    fun onClick(isRightClick: Boolean){
        if(sessionsMap.isEmpty()) return
        sessionsMap.values.forEach { it.onClick(isRightClick) }
    }

    fun onChunkUnload(x: Int, z: Int): Boolean{
        return sessionsMap.values.any {
            it.onChunkUnload(x, z)
        }
    }


    companion object{
        private val playerMap = HashMap<UUID, HoverUI>()
        val Player.hoverUI: HoverUI
            get() = get(this)
        @JvmStatic
        operator fun get(player: Player): HoverUI{
            return playerMap[player.uniqueId]?.takeIf {
                val isPlayer = it.player == player
                if(!isPlayer){
                    it.destroy()
                }
                isPlayer
            } ?: run {
                val hoverUI = HoverUI(player)
                playerMap[player.uniqueId] = hoverUI
                hoverUI
            }
        }
    }
}

