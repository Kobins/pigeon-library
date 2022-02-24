package kr.lostwar.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class Keybind(val key: String){
    JUMP("key.jump"),
    SNEAK("key.sneak"),
    SPRINT("key.sprint"),
    LEFT("key.left"),
    RIGHT("key.right"),
    BACK("key.back"),
    FORWARD("key.forward"),
    ATTACK("key.attack"),
    PICK_ITEM("key.pickItem"),
    USE("key.use"),
    DROP("key.drop"),
    HOTBAR_1("key.hotbar.1"),
    HOTBAR_2("key.hotbar.2"),
    HOTBAR_3("key.hotbar.3"),
    HOTBAR_4("key.hotbar.4"),
    HOTBAR_5("key.hotbar.5"),
    HOTBAR_6("key.hotbar.6"),
    HOTBAR_7("key.hotbar.7"),
    HOTBAR_8("key.hotbar.8"),
    HOTBAR_9("key.hotbar.9"),
    INVENTORY("key.inventory"),
    SWAP_HANDS("key.swapOffhand"),
    LOAD_TOOLBAR_ACTIVATOR("key.loadToolbarActivator"),
    SAVE_TOOLBAR_ACTIVATOR("key.saveToolbarActivator"),
    PLAYERLIST("key.playerlist"),
    CHAT("key.chat"),
    COMMAND("key.command"),
    SOCIAL_INTERACTIONS("key.socialInteractions"),
    ADVANCEMENTS("key.advancements"),
    SPECTATOR_OUTLINES("key.spectatorOutlines"),
    SCREENSHOT("key.screenshot"),
    SMOOTH_CAMERA("key.smoothCamera"),
    FULLSCREEN("key.fullscreen"),
    TOGGLE_PERSPECTIVE("key.togglePerspective");

    val asComponent = keybind(key)

    companion object{
        fun List<Keybind>.join(
            delimeter: Component = text(" + "),
            prefix: Component = empty(),
            suffix: Component = empty(),
            color: TextColor = NamedTextColor.GOLD,
        ) = text()
            .append(prefix.color(color))
            .append(if(prefix == empty()) empty() else space())
            .append(join(delimeter.color(color), map { it.asComponent.color(NamedTextColor.WHITE) }))
            .append(if(suffix == empty()) empty() else space())
            .append(suffix.color(color))
    }
}