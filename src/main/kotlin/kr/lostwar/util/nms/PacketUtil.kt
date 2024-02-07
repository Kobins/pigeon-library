package kr.lostwar.util.nms

import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap
import kr.lostwar.util.math.VectorUtil.toYawPitchDegree
import kr.lostwar.util.nms.NMSUtil.asNMSCopy
import kr.lostwar.util.nms.NMSUtil.nmsEntity
import kr.lostwar.util.nms.NMSUtil.nmsPlayer
import kr.lostwar.util.nms.NMSUtil.nmsSlot
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.RelativeMovement
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashMap

object PacketUtil {


    private val relativeAll = setOf(
        RelativeMovement.X,
        RelativeMovement.Y,
        RelativeMovement.Z,
        RelativeMovement.X_ROT,
        RelativeMovement.Y_ROT,
    )
    private val relativeMove = setOf(
        RelativeMovement.X,
        RelativeMovement.Y,
        RelativeMovement.Z,
    )

    fun Player.sendPacket(packet: Packet<*>) = nmsPlayer.connection.send(packet)

    fun Player.rotateCamera(yaw: Float, pitch: Float){
        val packet = ClientboundPlayerPositionPacket(0.0, 0.0, 0.0, yaw, pitch, relativeAll, 0)
        sendPacket(packet)
    }
    fun Player.setEyeDirection(direction: Vector){
        val (yaw, pitch) = direction.toYawPitchDegree()
        if(yaw.isNaN() || pitch.isNaN()){
            return;
        }
        setEyeDirection(yaw, pitch)
    }
    fun Player.setEyeDirection(yaw: Float, pitch: Float){
        val packet = ClientboundPlayerPositionPacket(0.0, 0.0, 0.0, yaw, pitch, relativeMove, 0)
        sendPacket(packet)
    }

    fun Player.setCamera(entity: Entity){
        val nmsEntity = entity.nmsEntity ?: return
        val packet = ClientboundSetCameraPacket(nmsEntity)
        sendPacket(packet)
    }
    fun Player.resetCamera(){
        setCamera(this)
    }

    fun Player.resetCooldown(material: Material) {
        val nmsPlayer = nmsPlayer
        nmsPlayer.cooldowns.removeCooldown(CraftMagicNumbers.getItem(material))
    }

    fun Player.sendEquipment(itemStack: ItemStack, slot: EquipmentSlot, target: Iterable<Player> = world.players) {
        val packet = ClientboundSetEquipmentPacket(entityId, listOf(
            com.mojang.datafixers.util.Pair(slot.nmsSlot, itemStack.asNMSCopy())
        ))

        for(player in target) {
            player.sendPacket(packet)
        }
    }
    fun Player.sendEquipmentSelf(itemStack: ItemStack, slot: EquipmentSlot)
        = sendEquipment(itemStack, slot, Collections.singletonList(this))

    private val sectionBlocksUpdatePacketConstructor = ReflectionUtil.getConstructor(ClientboundSectionBlocksUpdatePacket::class.java,
        SectionPos::class.java,
        Short2ObjectMap::class.java,
        Boolean::class.java,
    )
    fun Iterable<Player>.sendMultiBlockChange(blockChanges: Map<Location, BlockData>, suppressLightUpdates: Boolean = false) {
        val sectionMap = HashMap<SectionPos, Short2ObjectMap<BlockState>>()
        for((location, blockData) in blockChanges) {
            val blockPos = BlockPos(location.x.toInt(), location.y.toInt(), location.z.toInt())
            val sectionPos = SectionPos.of(blockPos)

            val sectionData = sectionMap.getOrPut(sectionPos) { Short2ObjectArrayMap() }
            sectionData[SectionPos.sectionRelativePos(blockPos)] = (blockData as CraftBlockData).state
        }
        val packets = buildList<ClientboundSectionBlocksUpdatePacket> {
            for((sectionPos, blockData) in sectionMap) {
                add(sectionBlocksUpdatePacketConstructor
                    .newInstance(sectionPos, blockData, suppressLightUpdates) as ClientboundSectionBlocksUpdatePacket
                )
            }
        }
        for(player in this) {
            for(packet in packets) {
                player.sendPacket(packet)
            }
        }
    }

//var PacketContainer.bossBarAction: BossBarAction
//    get() = getEnumModifier(BossBarAction::class.java, 1).read(0)
//    set(value) {
//        val action = modifier.read(1) as PacketPlayOutBoss.b
//        getEnumModifier(BossBarAction::class.java, 1).write(0, value)
//    }
//
//private val PacketContainer.bossBarColorIndex
//    get() = if(bossBarAction == UPDATE_STYLE) 4 else 2
//var PacketContainer.bossBarColor: BarColor
//    get() = getEnumModifier(BarColor::class.java, bossBarColorIndex).read(0)
//    set(value) {
//        getEnumModifier(BarColor::class.java, bossBarColorIndex).write(0, value)
//    }
//
//private val PacketContainer.bossBarStyleIndex
//    get() = if(bossBarAction == UPDATE_STYLE) 5 else 2
//var PacketContainer.bossBarStyle: BarStyle
//    get() = getEnumModifier(BarStyle::class.java, bossBarStyleIndex).read(0)
//    set(value) {
//        getEnumModifier(BarStyle::class.java, bossBarStyleIndex).write(0, value)
//    }

//    enum class BossBarAction {
//        ADD, REMOVE, UPDATE_PCT, UPDATE_NAME, UPDATE_STYLE, UPDATE_PROPERTIES
//    }
}