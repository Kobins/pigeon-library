package kr.lostwar.util.ui.top

import kr.lostwar.util.nms.NMSUtil.toNMS
import kr.lostwar.util.ui.top.TopUI.Companion.toNMS
import net.kyori.adventure.bossbar.BossBar
import net.minecraft.world.BossEvent

class TopUIBossEvent(topUI: TopUI)
    : BossEvent(topUI.uniqueId, topUI.name.toNMS(), topUI.color.toNMS(), topUI.overlay.toNMS())
{
    init {
        this.progress = topUI.progress.toFloat()
        darkenScreen = BossBar.Flag.DARKEN_SCREEN in topUI.flags
        playBossMusic = BossBar.Flag.PLAY_BOSS_MUSIC in topUI.flags
        createWorldFog = BossBar.Flag.CREATE_WORLD_FOG in topUI.flags
    }

    fun update(topUI: TopUI) {
        setName(topUI.name.toNMS())
        setColor(topUI.color.toNMS())
        setOverlay(topUI.overlay.toNMS())
        setProgress(topUI.progress.toFloat())
        darkenScreen = BossBar.Flag.DARKEN_SCREEN in topUI.flags
        playBossMusic = BossBar.Flag.PLAY_BOSS_MUSIC in topUI.flags
        createWorldFog = BossBar.Flag.CREATE_WORLD_FOG in topUI.flags
    }
}