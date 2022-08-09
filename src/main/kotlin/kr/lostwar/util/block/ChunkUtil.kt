package kr.lostwar.util.block

import org.bukkit.Chunk
import org.bukkit.Location

object ChunkUtil {

    val Location.chunkKey: Long; get() = Chunk.getChunkKey(this)

}