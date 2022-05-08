package kr.lostwar.util

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Pair
import kr.lostwar.PigeonLibraryPlugin
import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.*
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*


class FakeArmorStand(location: Location, private val headRotateByPose: Boolean = false) {

    private val observers: MutableList<Player> = Lists.newArrayList()
    val nmsArmorStand: ArmorStand
    init {
        val worldServer = (location.world as CraftWorld).handle
        nmsArmorStand = ArmorStand(worldServer, location.x, location.y, location.z).apply {
            if(!headRotateByPose){
                yRot = location.yaw
            }
            isNoGravity = true
        }
    }
    private val serverLocation: Location
        get() = Location(nmsArmorStand.level.minecraftWorld.world,
            nmsArmorStand.x,
            nmsArmorStand.y,
            nmsArmorStand.z,
            nmsArmorStand.yRot,
            nmsArmorStand.xRot,
        )
    private var lastLocation = location.clone()
    private var changed = false

    // Update task
    private val task: BukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PigeonLibraryPlugin.instance, Runnable { tick() }, 1, 1)
    fun addObserver(player: Player) {
        val spawned = ClientboundAddEntityPacket(nmsArmorStand)
        player.sendPacket(spawned)
        updateEntity(player)
        updateEntityHead(player)
        observers.add(player)
    }

    fun removeObserver(player: Player) {
        val destroyPacket = ClientboundRemoveEntitiesPacket(nmsArmorStand.id)
        player.sendPacket(destroyPacket)
        observers.remove(player)
    }

    private fun tick() {
        // Detect changes
        val serverLocation = serverLocation
        if (changed) {
            for (player in observers) {
                updateEntity(player)
            }
            changed = false

            // Update location
        }
        if (serverLocation != lastLocation) {
            broadcastMoveEntity(serverLocation)
            lastLocation = serverLocation
        }
    }

    private fun updateEntity(player: Player) {
        val dataPacket = ClientboundSetEntityDataPacket(nmsArmorStand.id, nmsArmorStand.entityData, true)
        player.sendPacket(dataPacket)
    }

    private fun updateEntityHead(player: Player){
        val map = listOf(Pair(EquipmentSlot.HEAD, nmsArmorStand.getItemBySlot(EquipmentSlot.HEAD)))
        val equipmentPacket = ClientboundSetEquipmentPacket(nmsArmorStand.id, map)
        player.sendPacket(equipmentPacket)
    }

    private fun broadcastMoveEntity(newLocation: Location) {
        val move = newLocation - lastLocation
        val distance = move.lengthSquared()
        if(distance < 64){
            val dx = (move.x * 4096).toInt().toShort()
            val dy = (move.y * 4096).toInt().toShort()
            val dz = (move.z * 4096).toInt().toShort()
            val yaw = (newLocation.yaw * 256f / 300f).toInt().toByte()
            val pitch = (newLocation.pitch * 256f / 300f).toInt().toByte()
            val relMove = ClientboundMoveEntityPacket.PosRot(
                nmsArmorStand.id,
                dx, dy, dz, yaw, pitch, false
            )
            for (player in observers) {
                player.sendPacket(relMove)
            }
        }else{
            val teleport = ClientboundTeleportEntityPacket(nmsArmorStand)
            for (player in observers) {
                player.sendPacket(teleport)
            }
        }
    }



    /**
     * Destroy the current entity.
     */
    fun destroy() {
        task.cancel()
        for (player in Lists.newArrayList(observers)) {
            removeObserver(player)
        }
    }

    /**
     * Retrieve an immutable view of every player observing this entity.
     * @return Every observer.
     */
    fun getObservers(): List<Player> {
        return Collections.unmodifiableList(observers)
    }

    var location: Location = serverLocation
        set(value) {
            if(field != value){
                changed = true
//                value.world.spawnParticle(Particle.HEART, value, 1, 0.0, 0.0, 0.0)
            }
            field = value
            if(headRotateByPose) {
                nmsArmorStand.absMoveTo(location.x, location.y - nmsArmorStand.eyeHeight, location.z, 0f, 0f)
                nmsArmorStand.setYHeadRot(0f)
                nmsArmorStand.setHeadPose(Rotations(location.pitch, location.yaw, 0f))
            }else{
                nmsArmorStand.absMoveTo(location.x, location.y - nmsArmorStand.eyeHeight, location.z, location.yaw, 0f)
                nmsArmorStand.setYHeadRot(location.yaw)
                nmsArmorStand.setHeadPose(Rotations(location.pitch, 0f, 0f))
            }
        }

    var displayName: String? = null
        set(value) {
            if(field != value){
                changed = true
            }
            field = value
            nmsArmorStand.customName = CraftChatMessage.fromStringOrNull(value)
            nmsArmorStand.isCustomNameVisible = value != null && value.isNotBlank()
        }

    var head: ItemStack = ItemStack(AIR)
        set(value) {
            val update = field != value
            field = value
            nmsArmorStand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(value))
            if(update) {
                for(player in observers) {
                    updateEntityHead(player)
                }
            }
        }

    var isVisible: Boolean = true
        set(value) {
            val update = field != value
            field = value
            nmsArmorStand.isInvisible = !value
            if(update) {
                for(player in observers) updateEntity(player)
            }
        }

    var isMarker: Boolean = false
        set(value) {
            val update = field != value
            field = value
            nmsArmorStand.isMarker = value
            if(update) {
                for(player in observers) updateEntity(player)
            }
        }
}