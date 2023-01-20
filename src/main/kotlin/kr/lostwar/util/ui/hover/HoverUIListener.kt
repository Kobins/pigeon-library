package kr.lostwar.util.ui.hover

import kr.lostwar.util.ui.hover.HoverUI.Companion.hoverUI
import kr.lostwar.util.ui.text.console
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkUnloadEvent

object HoverUIListener :
//    PacketAdapter(
//        PigeonLibraryPlugin.instance,
//        PacketType.Play.Server.UNLOAD_CHUNK
//    ),
    Listener
{

//    override fun onPacketReceiving(event: PacketEvent) {
//    }

//    override fun onPacketSending(event: PacketEvent) {
//        if(event.isPlayerTemporary) return
//        val player = event.player
//        val packet = event.packet
//        val chunkX = packet.integers.read(0)
//        val chunkZ = packet.integers.read(1)
//        event.isCancelled = HoverUI[player].onChunkUnload(chunkX, chunkZ)
//        if(event.isCancelled){
//            console("chunk unload cancelled by HoverUIListener($chunkX, $chunkZ)")
//        }
//    }

    @EventHandler
    fun ChunkUnloadEvent.onUnload(){
        val chunkX = chunk.x
        val chunkZ = chunk.z
        Bukkit.getOnlinePlayers().forEach {
            if(it.hoverUI.onChunkUnload(chunkX, chunkZ)){
                console("&cchunk unload detected by HoverUIListener&e($chunkX, $chunkZ)")
            }
        }
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteract(){
        HoverUI[player].onClick(true)
    }
    @EventHandler
    fun PlayerInteractEntityEvent.onInteract(){
        HoverUI[player].onClick(true)
    }
    @EventHandler
    fun PlayerInteractEvent.onInteract(){
        val isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK
        HoverUI[player].onClick(isRightClick)
    }
}