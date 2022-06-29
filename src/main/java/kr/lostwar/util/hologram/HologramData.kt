package kr.lostwar.util.hologram

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.ArmorStand

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
    var component: Component = Component.empty()
    set(value) {
        field = value
        armorStand.isCustomNameVisible = value != Component.empty()
        armorStand.customName(value)
//        Main.console("  hologram [$index] setted to $name")
    }
}