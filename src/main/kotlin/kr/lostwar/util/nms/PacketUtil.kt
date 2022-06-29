package kr.lostwar.util.nms

import kr.lostwar.util.math.VectorUtil.toYawPitch
import kr.lostwar.util.nms.NMSUtil.nmsEntity
import kr.lostwar.util.nms.NMSUtil.nmsPlayer
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object PacketUtil {


    private val relativeAll = setOf(
        ClientboundPlayerPositionPacket.RelativeArgument.X,
        ClientboundPlayerPositionPacket.RelativeArgument.Y,
        ClientboundPlayerPositionPacket.RelativeArgument.Z,
        ClientboundPlayerPositionPacket.RelativeArgument.X_ROT,
        ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT,
    )
    private val relativeMove = setOf(
        ClientboundPlayerPositionPacket.RelativeArgument.X,
        ClientboundPlayerPositionPacket.RelativeArgument.Y,
        ClientboundPlayerPositionPacket.RelativeArgument.Z,
    )

    fun Player.sendPacket(packet: Packet<*>) = nmsPlayer.connection.send(packet)

    fun Player.rotateCamera(yaw: Float, pitch: Float){
        val packet = ClientboundPlayerPositionPacket(0.0, 0.0, 0.0, yaw, pitch, relativeAll, 0, false)
        sendPacket(packet)
    }
    fun Player.setEyeDirection(direction: Vector){
        val (yaw, pitch) = direction.toYawPitch()
        if(yaw.isNaN() || pitch.isNaN()){
            return;
        }
        setEyeDirection(yaw, pitch)
    }
    fun Player.setEyeDirection(yaw: Float, pitch: Float){
        val packet = ClientboundPlayerPositionPacket(0.0, 0.0, 0.0, yaw, pitch, relativeMove, 0, false)
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