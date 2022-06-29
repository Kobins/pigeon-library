package kr.lostwar.util

import org.bukkit.Location
import kotlin.math.floor

object ChunkUtil {
    fun Location.chunkLoaded(): Boolean{
        if(!isWorldLoaded){
            return false
        }
        val world = world ?: return false
        return world.isChunkLoaded(floor(x).toInt() shr 4, floor(z).toInt() shr 4);
    }

}