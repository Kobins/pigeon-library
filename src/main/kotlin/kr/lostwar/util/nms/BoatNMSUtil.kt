package kr.lostwar.util.nms

import kr.lostwar.util.nms.NMSUtil.nmsEntity
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import org.bukkit.entity.Entity
import kotlin.math.max

object BoatNMSUtil {

    enum class BoatState {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR,
    }

    fun Entity.getGroundFriction(epsilon: Double = 0.01): Float {
        val nmsEntity = nmsEntity ?: return 0f
        val aabb = nmsEntity.boundingBox
        val lowerAABB = aabb.setMinY(aabb.minY - epsilon)
        val minX = Mth.floor(lowerAABB.minX) - 1
        val maxX = Mth.ceil(lowerAABB.maxX) + 1
        val minY = Mth.floor(lowerAABB.minY) - 1
        val maxY = Mth.ceil(lowerAABB.maxY) + 1
        val minZ = Mth.floor(lowerAABB.minZ) - 1
        val maxZ = Mth.ceil(lowerAABB.maxZ) + 1
        val shape = Shapes.create(lowerAABB)
        var frictionSum = 0.0f
        var frictionCount = 0
        val v = MutableBlockPos()
        val level = nmsEntity.level

        for(x in minX until maxX) {
            for(z in minZ until maxZ) {
                val flag = (if(x != minX && x != maxX - 1) 0 else 1) +
                           (if(z != minZ && z != maxZ - 1) 0 else 1)
                if(flag != 2) {
                    for(y in minY until maxY) {
                        if(flag <= 0 || y != minY && y != maxY - 1) {
                            v.set(x, y, z)
                            val b = nmsEntity.level.getBlockState(v)

                            if(Shapes.joinIsNotEmpty(b.getCollisionShape(level, v).move(x.toDouble(), y.toDouble(), z.toDouble()), shape, BooleanOp.AND)) {
                                frictionSum += b.block.friction
                                ++frictionCount
                            }
                        }
                    }
                }
            }
        }
        return frictionSum / frictionCount
    }

    fun Entity.getWaterLevel(epsilon: Double = 0.1): Pair<Double, Boolean> {
        val nmsEntity = nmsEntity ?: return 0.0 to false
        val aabb = nmsEntity.boundingBox
        val minX = Mth.floor(aabb.minX)
        val maxX = Mth.ceil(aabb.maxX)
        val minY = Mth.floor(aabb.minY)
        val maxY = Mth.ceil(aabb.minY + epsilon)
        val minZ = Mth.floor(aabb.minZ)
        val maxZ = Mth.ceil(aabb.maxZ)

        var maxWaterLevel = -1.7976931348623157E308
        val v = MutableBlockPos()
        val level = nmsEntity.level

        for(x in minX until maxX) {
            for(z in minZ until maxZ) {
                for(y in minY until maxY) {
                    v.set(x, y, z)
                    val fluid = level.getFluidState(v)
                    if(fluid.`is`(FluidTags.WATER)) {
                        val waterLevel = y + fluid.getHeight(level, v)

                        maxWaterLevel = max(waterLevel.toDouble(), maxWaterLevel)
                    }
                }
            }
        }
        return maxWaterLevel to (aabb.minY < maxWaterLevel)
    }

    fun Entity.getWaterLevelAbove(dy: Double): Double {
        val nmsEntity = nmsEntity ?: return 0.0
        val aabb = nmsEntity.boundingBox
        val minX = Mth.floor(aabb.minX)
        val maxX = Mth.ceil(aabb.maxX)
        val minY = Mth.floor(aabb.maxY)
        val maxY = Mth.ceil(aabb.maxY - dy)
        val minZ = Mth.floor(aabb.minZ)
        val maxZ = Mth.ceil(aabb.maxZ)

        val v = MutableBlockPos()
        val level = nmsEntity.level

        yLoop@
        for(y in minY until maxY) {
            var height = 0.0
            for(x in minX until maxX) {
                for(z in minZ until maxZ) {
                    v.set(x, y, z)
                    val fluid = level.getFluidState(v)
                    if(fluid.`is`(FluidTags.WATER)) {
                        val waterLevel = y + fluid.getHeight(level, v).toDouble()

                        height = max(waterLevel, height)
                        if(height >= 1.0f) {
                            continue@yLoop
                        }
                    }
                }
            }
            if(height < 1.0) {
                return v.y + height
            }
        }
        return maxY + 1.0
    }

    fun Entity.isUnderWaterAndGetBoatState(): BoatState? {
        val nmsEntity = nmsEntity
        val aabb = nmsEntity.boundingBox
        val upper = aabb.maxY + 0.001
        val minX = Mth.floor(aabb.minX)
        val maxX = Mth.ceil(aabb.maxX)
        val minY = Mth.floor(aabb.maxY)
        val maxY = Mth.ceil(upper)
        val minZ = Mth.floor(aabb.minZ)
        val maxZ = Mth.ceil(aabb.maxZ)

        var flag = false
        val v = MutableBlockPos()
        val level = nmsEntity.level
        var fluid: FluidState
        for(x in minX until maxX) {
            for(z in minZ until maxZ) {
                for(y in minY until maxY) {
                    v.set(x, y, z)
                    fluid = level.getFluidState(v)
                    if(fluid.`is`(FluidTags.WATER) && upper < (v.y + fluid.getHeight(level, v))) {
                        if(!fluid.isSource) {
                            return BoatState.UNDER_FLOWING_WATER
                        }
                        flag = true
                    }
                }
            }
        }
        return if(flag) BoatState.UNDER_WATER else null
    }


}