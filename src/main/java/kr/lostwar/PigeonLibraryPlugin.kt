package kr.lostwar

import kr.lostwar.util.ui.text.console
import org.bukkit.plugin.java.JavaPlugin

class PigeonLibraryPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }
    override fun onEnable() {
        instance = this
        console("&apigeon-library enabled")
    }

    override fun onDisable() {
        console("&cpigeon-library disabled")
    }


}