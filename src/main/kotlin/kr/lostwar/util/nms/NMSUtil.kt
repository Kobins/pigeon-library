package kr.lostwar.util.nms

import kr.lostwar.util.math.VectorUtil
import kr.lostwar.util.math.VectorUtil.normalized
import kr.lostwar.util.math.VectorUtil.times
import kr.lostwar.util.nms.PacketUtil.sendPacket
import kr.lostwar.util.ui.ComponentUtil.toJSONString
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.bukkit.FluidCollisionMode
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_19_R1.CraftFluidCollisionMode
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage
import org.bukkit.craftbukkit.v1_19_R1.util.CraftRayTraceResult
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

object NMSUtil {

    val Player.nmsPlayer: ServerPlayer; get() = (this as CraftPlayer).handle
    val Entity.nmsEntity; get() = (this as? CraftEntity)?.handle ?: error("tried get NMS Entity, but it was not instance of CraftEntity")
    val LivingEntity.nmsLivingEntity; get() = (this as? CraftLivingEntity)?.handle
    val World.nmsWorld: ServerLevel; get() = (this as CraftWorld).handle
    fun ItemStack.asNMSCopy(): net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(this)
    fun String?.toNMSComponent(): net.minecraft.network.chat.Component = CraftChatMessage.fromStringOrNull(this)
    fun Component.toNMSComponent(): net.minecraft.network.chat.Component = CraftChatMessage.fromJSON(toJSONString())
    fun Player.getExpToDrop(block: Block): Int {
        val pos = BlockPos(block.x, block.y, block.z)
        val level = world.nmsWorld
        val nmsData = level.getBlockState(pos)
        val nmsBlock = nmsData.block ?: return 0

        val item = inventory.itemInMainHand

        if (block.isPreferredTool(item)) {
            return nmsBlock.getExpDrop(nmsData, level, pos, item.asNMSCopy(), true)
        }
        return 0

    }
    fun Component.toNMS(): net.minecraft.network.chat.Component {
        return net.minecraft.network.chat.Component.Serializer.fromJson(toJSONString())!!
    }

    private val equipmentSlotBukkitToNMS = mapOf(
        EquipmentSlot.HAND      to net.minecraft.world.entity.EquipmentSlot.MAINHAND,
        EquipmentSlot.OFF_HAND  to net.minecraft.world.entity.EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD      to net.minecraft.world.entity.EquipmentSlot.HEAD,
        EquipmentSlot.CHEST     to net.minecraft.world.entity.EquipmentSlot.CHEST,
        EquipmentSlot.LEGS      to net.minecraft.world.entity.EquipmentSlot.LEGS,
        EquipmentSlot.FEET      to net.minecraft.world.entity.EquipmentSlot.FEET,
    )
    val EquipmentSlot.nmsSlot; get() = equipmentSlotBukkitToNMS[this]!!

    // 1.19.1 obfucscation https://piston-data.mojang.com/v1/objects/3565648cdd47ae15738fb804a95a659137d7cfd3/server.txt

    private val entityEyeHeightField = ReflectionUtil.getField(net.minecraft.world.entity.Entity::class.java, "ba")
    private val entityDimensionsField = ReflectionUtil.getField(net.minecraft.world.entity.Entity::class.java, "aZ")
    private val entityHardCollidesField = ReflectionUtil.getField(net.minecraft.world.entity.Entity::class.java, "hardCollides")

    fun org.bukkit.entity.Entity.setEntitySize(width: Float, height: Float, eye: Float? = null) {
        val nmsEntity = nmsEntity
        // 1.19
        if(eye != null)
            entityEyeHeightField.setFloat(nmsEntity, eye)
        entityDimensionsField.set(nmsEntity, EntityDimensions(width, height, false))
    }

    private val collideMethod = ReflectionUtil.getMethod(net.minecraft.world.entity.Entity::class.java, "g", Vec3::class.java)
    fun org.bukkit.entity.Entity.tryCollideAndGetModifiedVelocity(velocity: Vector): Vector {
        val nmsEntity = nmsEntity
        val vec = Vec3(velocity.x, velocity.y, velocity.z)
        val resultVec = collideMethod.invoke(nmsEntity, vec) as Vec3
        return Vector(resultVec.x, resultVec.y, resultVec.z)
    }

    fun org.bukkit.entity.Entity.setPosition(position: Vector) {
        val nmsEntity = nmsEntity
        nmsEntity.setPos(position.x, position.y, position.z)
    }

    fun org.bukkit.entity.Entity.setNoPhysics(noPhysics: Boolean) {
        val nmsEntity = nmsEntity
        nmsEntity.noPhysics = noPhysics
    }

    fun org.bukkit.entity.Entity.setHardCollides(hardCollides: Boolean) {
        val nmsEntity = nmsEntity
        entityHardCollidesField.setBoolean(nmsEntity, hardCollides)
    }
    fun org.bukkit.entity.Entity.isHardCollides(): Boolean {
        val nmsEntity = nmsEntity
        return entityHardCollidesField.getBoolean(nmsEntity)
    }

    fun org.bukkit.entity.Entity.setIsOnGround(onGround: Boolean) {
        val nmsEntity = nmsEntity
        nmsEntity.isOnGround = onGround
    }

    fun org.bukkit.entity.Entity.setImpulse() {
        val nmsEntity = nmsEntity
        nmsEntity.hasImpulse = true
    }

    fun org.bukkit.entity.Entity.setMaxUpStep(maxUpStep: Float) {
        val nmsEntity = nmsEntity
        nmsEntity.maxUpStep = maxUpStep
    }

    fun org.bukkit.entity.Entity.hasVerticalCollision(): Boolean {
        val nmsEntity = nmsEntity
        return nmsEntity.verticalCollision
    }
    fun org.bukkit.entity.Entity.hasVerticalCollisionBelow(): Boolean {
        val nmsEntity = nmsEntity
        return nmsEntity.verticalCollisionBelow
    }

    fun org.bukkit.entity.LivingEntity.setDiscardFriction(discardFriction: Boolean) {
        val nmsEntity = nmsLivingEntity ?: return
        nmsEntity.setDiscardFriction(discardFriction)
    }

    fun org.bukkit.entity.LivingEntity.travel(movementInput: Vector = VectorUtil.ZERO) {
        val nmsEntity = nmsLivingEntity ?: return
        nmsEntity.travel(Vec3(movementInput.x, movementInput.y, movementInput.z))
    }

    private fun DamageCause.toNMS() = when(this) {
        DamageCause.CUSTOM -> DamageSource.GENERIC
        DamageCause.FIRE -> DamageSource.IN_FIRE
        DamageCause.FIRE_TICK -> DamageSource.ON_FIRE
        DamageCause.STARVATION -> DamageSource.STARVE
        DamageCause.WITHER -> DamageSource.WITHER
        DamageCause.DROWNING -> DamageSource.DROWN
        DamageCause.MELTING -> CraftEventFactory.MELTING
        DamageCause.POISON -> CraftEventFactory.POISON
        DamageCause.MAGIC -> DamageSource.MAGIC
        DamageCause.FALL -> DamageSource.FALL
        DamageCause.FLY_INTO_WALL -> DamageSource.FLY_INTO_WALL
        DamageCause.CRAMMING -> DamageSource.CRAMMING
        DamageCause.DRYOUT -> DamageSource.DRY_OUT
        DamageCause.FREEZE -> DamageSource.FREEZE
        DamageCause.FALLING_BLOCK -> DamageSource.FALLING_BLOCK // fixme
        DamageCause.LIGHTNING -> DamageSource.LIGHTNING_BOLT // fixme?
        DamageCause.DRAGON_BREATH -> DamageSource.DRAGON_BREATH
        DamageCause.CONTACT -> DamageSource.CACTUS // fixme
        DamageCause.HOT_FLOOR -> DamageSource.HOT_FLOOR
        else -> error("failed to parse DamageCause.${this} to NMS DamageSource")
    }
    fun org.bukkit.entity.Entity.damage(amount: Double, cause: DamageCause): Boolean {
        val nmsEntity = nmsEntity
        return nmsEntity.hurt(cause.toNMS(), amount.toFloat())
    }

    enum class RayTraceContinuation {
        STOP,
        PIERCE,
    }
    fun World.rayTraceBlocksPiercing(
        origin: Vector,
        direction: Vector,
        maxDistance: Double,
        fluidCollisionMode: FluidCollisionMode,
        ignorePassableBlocks: Boolean = true,
        onHit: (distance: Double, result: RayTraceResult?) -> RayTraceContinuation,
    ) {
//        console("rayTraceBlocksPiercing(${origin}, ${direction}, ${maxDistance}, ${fluidCollisionMode}, ${ignorePassableBlocks})")
        val nmsWorld = nmsWorld
        if(maxDistance < 0.0) {
//            console("- invalid maxDistance, return")
            return
        }
        val normalizedDirection = direction.normalized
        val dir = normalizedDirection.times(maxDistance)
        val start = Vec3(origin.x, origin.y, origin.z)
        val end = Vec3(origin.x + dir.x, origin.y + dir.y, origin.z + dir.z)
//        console("- start: ${start}, end: ${end}")
        val context = ClipContext(
            start, end,
            if(ignorePassableBlocks) ClipContext.Block.COLLIDER else ClipContext.Block.OUTLINE,
            CraftFluidCollisionMode.toNMS(fluidCollisionMode),
            null
        )
        traverseBlocks(start, end, context,
            onMove = { c, blockPos ->
                val nmsResult = nmsWorld.clip(c, blockPos)
                if(nmsResult == null){
//                    console("onMove(${blockPos}): result is null")
                    return@traverseBlocks null
                }
                val result = CraftRayTraceResult.fromNMS(this, nmsResult)
                val distance = if(result != null) normalizedDirection.dot(result.hitPosition.subtract(origin)) else maxDistance
//                console("onMove(${blockPos}): $result, distance=${distance}")
                if(onHit(distance, result) == RayTraceContinuation.STOP) {
//                    console("! stopped traverse")
                    // 중단
                    return@traverseBlocks nmsResult
                }
                // 스킵
                null
            },
            onMiss = {
                val d = it.from.subtract(it.to)
                onHit(maxDistance, null)
                BlockHitResult.miss(it.to, Direction.getNearest(d.x, d.y, d.z), BlockPos(it.to))
            }
        )
    }
    private const val epsilon = -1.0E-7
    // from BlockGetter.class
    private fun traverseBlocks(
        start: Vec3, end: Vec3, context: ClipContext,
        onMove: (context: ClipContext, blockPos: BlockPos) -> BlockHitResult?,
        onMiss: (context: ClipContext) -> BlockHitResult
    ): BlockHitResult {
//        console("traverseBlocks(${start}, ${end})")
        if(start == end){
//            console("- start == end, miss")
            return onMiss(context)
        }

        val endX = Mth.lerp(epsilon, end.x, start.x)
        val endY = Mth.lerp(epsilon, end.y, start.y)
        val endZ = Mth.lerp(epsilon, end.z, start.z)
        val startX = Mth.lerp(epsilon, start.x, end.x)
        val startY = Mth.lerp(epsilon, start.y, end.y)
        val startZ = Mth.lerp(epsilon, start.z, end.z)
        var xInt = Mth.floor(startX)
        var yInt = Mth.floor(startY)
        var zInt = Mth.floor(startZ)
//        console("- startInt($xInt, $yInt, $zInt)")
        val blockPos = BlockPos.MutableBlockPos(xInt, yInt, zInt)
        val initialResult = onMove(context, blockPos)
        if(initialResult != null) return initialResult

        val xSize = endX - startX
        val ySize = endY - startY
        val zSize = endZ - startZ
        val xIntMove = Mth.sign(xSize)
        val yIntMove = Mth.sign(ySize)
        val zIntMove = Mth.sign(zSize)
        val xMove = if(xIntMove == 0) Double.MAX_VALUE else xIntMove.toDouble() / xSize
        val yMove = if(yIntMove == 0) Double.MAX_VALUE else yIntMove.toDouble() / ySize
        val zMove = if(zIntMove == 0) Double.MAX_VALUE else zIntMove.toDouble() / zSize
        var x = xMove * (if(xIntMove > 0) 1.0 - Mth.frac(startX) else Mth.frac(startX))
        var y = yMove * (if(yIntMove > 0) 1.0 - Mth.frac(startY) else Mth.frac(startY))
        var z = zMove * (if(zIntMove > 0) 1.0 - Mth.frac(startZ) else Mth.frac(startZ))
//        console("- start($x, $y, $z)")


        var result: BlockHitResult? = null
        do {
            if(x > 1.0 && y > 1.0 && z > 1.0) {
                return onMiss(context)
            }

            if(x < y) {
                if(x < z) {
                    xInt += xIntMove
                    x += xMove
                }else{
                    zInt += zIntMove
                    z += zMove
                }
            }else if(y < z) {
                yInt += yIntMove
                y += yMove
            }else{
                zInt += zIntMove
                z += zMove
            }
            result = onMove(context, blockPos.set(xInt, yInt, zInt))
        }while(result == null)

        return result
    }

    fun sendPrimaryLine(block: Block, player: Player, primaryLine: Component) {
        val craftBlock = (block as CraftBlock)
        val nmsBlock = craftBlock.handle
        val signEntity = nmsBlock.getBlockEntity(craftBlock.position, BlockEntityType.SIGN)
        if(!signEntity.isPresent) return
        val sign = signEntity.get()
        val packet = ClientboundBlockEntityDataPacket.create(sign) {
            it.saveWithFullMetadata().apply { putString("Text1", primaryLine.toJSONString()) }
        }
        player.sendPacket(packet)
    }

}

//import com.mojang.datafixers.DataFixUtils
//import net.minecraft.SharedConstants
//import net.minecraft.util.datafix.DataConverterRegistry
//import net.minecraft.util.datafix.fixes.DataConverterTypes
//import net.minecraft.world.entity.Entity
//import net.minecraft.world.entity.EntityTypes
//import net.minecraft.world.entity.EnumCreatureType
//import net.minecraft.world.level.World
//import java.lang.reflect.Type


//fun spawnEntity(entityTypes: EntityTypes<*>, location: Location): Entity?{
//    return entityTypes.spawnCreature(
//            (location.world as CraftWorld).handle,
//            null,
//            null,
//            BlockPosition(location.x, location.y, location.z),
//            EnumMobSpawn.COMMAND,
//            true,
//            false
//    )?.bukkitEntity
//}

//@Suppress("UNCHECKED_CAST")
//fun <T : Entity> injectNewEntity(
//        name: String,
//        extend_from: String,
//        function: (EntityTypes<T>, World) -> T,
//        function: EntityTypes.b<T>,
//        type: EnumCreatureType
//): EntityTypes<T>{
//    val dataTypes = DataConverterRegistry.a()
//            .getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().worldVersion))
//            .findChoiceType(DataConverterTypes.ENTITY)
//            .types() as MutableMap<Any, Type>
//    dataTypes["minecraft:$name"] = dataTypes["minecraft:$extend_from"]!!
//    val method = EntityTypes::class.java.getDeclaredMethod(
//            "a",
//            String::class.java,
//            EntityTypes.a::class.java
//    )
//    method.isAccessible = true
//    return method.invoke(null, name, EntityTypes.a.a<T>(function as EntityTypes.b<T>, type)) as EntityTypes<T>
//
//
//}