package kr.lostwar.util

import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.text.console
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable

//fun tickTimer(second: Int) = { hologram: Hologram, count: Int ->
//    if(count%20 == 0){
//        hologram.holograms[hologram.holograms.size-1].name = "${getTime(second - count/20)} 남음"
//    }
//    count/20 >= second
//}

data class HologramData(
        val index: Int,
        val armorStand: ArmorStand
){
    init {
        armorStand.isCustomNameVisible = false
    }

    var name: String
        get() = LegacyComponentSerializer.legacySection().serialize(component)
        set(value) { component = LegacyComponentSerializer.legacySection().deserialize(value) }
    var component: Component = empty()
    set(value) {
        field = value
        armorStand.isCustomNameVisible = value != empty()
        armorStand.customName(value)
//        Main.console("  hologram [$index] setted to $name")
    }
}

class Hologram(
        locations: List<Location>,
        val onTick: (Hologram, Int) -> Boolean
){

    constructor(location: Location, onTick: (Hologram, Int) -> Boolean) : this(listOf(location), onTick)

    val holograms = locations.mapIndexed { index, location ->
        HologramData(index, location.world.spawn(location, ArmorStand::class.java).apply {
            isMarker = true
            isVisible = true
            isInvisible = false
            isCustomNameVisible = true
            maxHealth = 100.0
            health = 100.0
            isInvulnerable = true
            setGravity(false)
            setAI(false)
            setBasePlate(false)
        })
    }.toMutableList()
    operator fun List<HologramData>.set(index: Int, name: String){
        get(index).name = name
    }

    operator fun set(index: Int, name: String){
        holograms[index].name = name
    }
    operator fun set(index: Int, component: Component){
        holograms[index].component = component
    }
    operator fun set(index: Int, location: Location){
        holograms[index].armorStand.teleport(location)
    }

    private var stop: Boolean = false

    init{
        object: BukkitRunnable(){
            var hologram: Hologram? = this@Hologram
            var count = 0
            override fun run() {
                if(stop || onTick(hologram!!, count++)){
                    holograms.forEach {
                        it.armorStand.remove()
                    }
                    holograms.clear()
                    hologram = null
                    cancel()
                    return
                }
                if(Bukkit.isStopping()){
                    stop()
                    return
                }
//                holograms.forEach {
//                    console("[${it.index}, ${it.armorStand.entityId}]: ${it.armorStand.health}/${it.armorStand.maxHealth}, isDead=${it.armorStand.isDead} => ${it.name}")
//                    if(it.armorStand.isDead){
//                        stop()
//                        return
//                    }
//                }
            }
        }.runTaskTimer(PigeonLibraryPlugin.instance, 0, 1)
    }

    fun stop(){
//        try {
//            throw Exception()
//        } catch (e: Exception){
//            e.printStackTrace()
//        }
        stop = true
    }
}