package kr.lostwar.util

import kr.lostwar.util.ComponentUtil.toJSONString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object NMSUtil {
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