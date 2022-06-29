package kr.lostwar.util.nms

import kr.lostwar.util.ui.ComponentUtil.toJSONString
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object NMSUtil {

    val Player.nmsPlayer: ServerPlayer; get() = (this as CraftPlayer).handle
    val Entity.nmsEntity; get() = (this as? CraftEntity)?.handle
    val World.nmsWorld: ServerLevel; get() = (this as CraftWorld).handle
    fun ItemStack.asNMSCopy(): net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(this)
    fun String?.toNMSComponent(): net.minecraft.network.chat.Component = CraftChatMessage.fromStringOrNull(this)
    fun Player.getExpToDrop(block: Block): Int {
        val pos = BlockPos(block.x, block.y, block.z)
        val level = world.nmsWorld
        val nmsData = level.getBlockState(pos)
        val nmsBlock = nmsData.block ?: return 0

        val item = inventory.itemInMainHand

        if (block.isPreferredTool(item)) {
            return nmsBlock.getExpDrop(nmsData, level, pos, item.asNMSCopy())
        }
        return 0

    }
    fun Component.toNMS(): net.minecraft.network.chat.Component {
        return net.minecraft.network.chat.Component.Serializer.fromJson(toJSONString())!!
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