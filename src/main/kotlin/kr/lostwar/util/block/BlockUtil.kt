package kr.lostwar.util.block

import org.bukkit.block.BlockFace

object BlockUtil {

    val BlockFace.simpleFace: BlockFace
        get() = when(this) {
            BlockFace.UP -> BlockFace.UP
            BlockFace.DOWN -> BlockFace.DOWN
            BlockFace.EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST -> BlockFace.EAST
            BlockFace.WEST, BlockFace.WEST_NORTH_WEST, BlockFace.WEST_SOUTH_WEST -> BlockFace.WEST
            BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST -> BlockFace.SOUTH
            BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST -> BlockFace.NORTH
            else -> BlockFace.UP
        }

    val BlockFace.left: BlockFace?
        get() = when(this) {
            BlockFace.NORTH -> BlockFace.WEST
            BlockFace.WEST -> BlockFace.SOUTH
            BlockFace.SOUTH -> BlockFace.EAST
            BlockFace.EAST -> BlockFace.NORTH
            else -> null
        }
    val BlockFace.right: BlockFace?
        get() = when(this) {
            BlockFace.NORTH -> BlockFace.EAST
            BlockFace.WEST -> BlockFace.NORTH
            BlockFace.SOUTH -> BlockFace.WEST
            BlockFace.EAST -> BlockFace.SOUTH
            else -> null
        }
}