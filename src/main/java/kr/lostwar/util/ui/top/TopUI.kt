package kr.lostwar.util.ui.top

import com.comphenix.protocol.PacketType.Play
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.nms.PacketUtil.sendPacket
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*


class TopUI(
    name: Component,
    color: BossBar.Color,
    overlay: BossBar.Overlay,
    vararg flags: BossBar.Flag
) : org.bukkit.boss.BossBar {
    enum class TopUIType{
        LEGACY,
        ADVENTURE;
    }

    val uniqueId = Mth.createInsecureUUID()
    var name: Component = name
        set(value) {
            if(field != value) {
                field = value
                broadcast(ClientboundBossEventPacket::createUpdateNamePacket)
            }
        }
    var color: BossBar.Color = color
        set(value) {
            if(field != value) {
                field = value
                broadcast(ClientboundBossEventPacket::createUpdateStylePacket)
            }
        }
    var overlay: BossBar.Overlay = overlay
        set(value) {
            if(field != value) {
                field = value
                broadcast(ClientboundBossEventPacket::createUpdateStylePacket)
            }
        }
    val flags = HashSet<BossBar.Flag>().apply { addAll(flags) }
    private var visible: Boolean = true
    private var progress: Double
    override fun getProgress() = progress
    override fun setProgress(progress: Double) { this.progress = progress }
    val playerSet = HashSet<Player>()
    init {
        progress = 0.0
    }
    constructor(
        title: String,
        color: BossBar.Color,
        style: BossBar.Overlay,
        vararg flags: BossBar.Flag
    ) : this(LegacyComponentSerializer.legacySection().deserialize(title.replace('&', '§')), color, style, *flags)
    constructor(bossBar: BossBar) : this(bossBar.name(), bossBar.color(), bossBar.overlay(), *bossBar.flags().toTypedArray()){
        progress = bossBar.progress().toDouble()
    }

    init {
        bossbarMap[this.uniqueId] = this
    }

    override fun getTitle() = name.toString()
    override fun setTitle(name: String?) {
        this.name = name?.let { LegacyComponentSerializer.legacySection().deserialize(it) } ?: empty()
    }

    override fun getPlayers() = playerSet.toList()

    override fun addFlag(flag: BarFlag) {
        if(flags.add(flag.toAdventure())) {
            broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket)
        }
    }
    override fun removeFlag(flag: BarFlag) {
        if(flags.remove(flag.toAdventure())) {
            broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket)
        }
    }
    override fun hasFlag(flag: BarFlag): Boolean {
        return flags.contains(flag.toAdventure())
    }

    override fun getColor() = color.toBukkit()
    override fun setColor(color: BarColor) {
        this.color = color.toAdventure()
    }

    override fun getStyle() = overlay.toBukkit()
    override fun setStyle(style: BarStyle) {
        this.overlay = style.toAdventure()
    }

    var isNoLongerUsed = false
        private set
    fun noLongerUsed(){
        isVisible = false
        removeAll()
        isNoLongerUsed = true
    }

    override fun isVisible(): Boolean = visible
    override fun setVisible(visible: Boolean) {
        if(visible != this.visible) {
            this.visible = visible
            bossEvent.update(this)
            val packet =
                if(visible) ClientboundBossEventPacket.createAddPacket(bossEvent)
                else ClientboundBossEventPacket.createRemovePacket(uniqueId)
            for(player in playerSet) {
                player.sendPacket(packet)
            }
        }
        if(!isVisible){ //이번에 끈 경우
            for(player in playerSet){
                player.removeBossBar()
            }
        }else{ //이번에 킨 경우
            for(player in playerSet){
                player.addBossBar()
            }
        }
    }

    override fun show() { isVisible = true }
    override fun hide() { isVisible = false }

    override fun addPlayer(player: Player) {
        if(playerSet.add(player) && isVisible) {
            bossEvent.update(this)
            player.sendPacket(ClientboundBossEventPacket.createAddPacket(bossEvent))
        }
        if(isVisible){
            player.addBossBar()
        }
    }

    override fun removePlayer(player: Player) { //제거할 때
        if(playerSet.remove(player) && isVisible) {
            player.sendPacket(ClientboundBossEventPacket.createRemovePacket(uniqueId))
        }
        player.removeBossBar()
    }

    override fun removeAll() {
        for(player in ArrayList(playerSet)) {
            removePlayer(player)
        }
    }

    private fun Player.addBossBar(){
        if(this@TopUI !in mutableBossbars){
            mutableBossbars.add(this@TopUI)
        }
    }

    private fun Player.removeBossBar(){
        if(this@TopUI in mutableBossbars){
            mutableBossbars.remove(this@TopUI)
        }
    }

    override fun hashCode(): Int {
        return this.uniqueId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other is TopUI){
            return this.uniqueId == other.uniqueId
        }

        return super.equals(other)
    }

    var componentTitle: Component
        get() = name
        set(value) { name = value }

    val bossEvent: TopUIBossEvent = TopUIBossEvent(this)
    internal fun broadcast(packetFactory: Function1<BossEvent, ClientboundBossEventPacket>) {
        if (isVisible) {
            bossEvent.update(this)
            val bossBarPacket = packetFactory.invoke(bossEvent)
            for(player in playerSet) {
                player.sendPacket(bossBarPacket)
            }
        }

    }
    companion object{

//        const val finalizeCount = 4
        init {
            object : BukkitRunnable() {
                override fun run() {
                    bossbarMap.entries.removeIf { it.value.isNoLongerUsed }
                }
            }.runTaskTimer(PigeonLibraryPlugin.instance, 0, 20 * 60)
        }

        @JvmStatic
        fun create(
            title: String,
            color: BarColor,
            style: BarStyle,
            vararg flags: BarFlag
        ) = TopUI(title, color.toAdventure(), style.toAdventure(), *flags.map { it.toAdventure() }.toTypedArray())

        @JvmStatic
        fun createWithAdventure(adventureBossBar: BossBar) = TopUI(adventureBossBar)

        private val bossbarMap = HashMap<UUID, TopUI>()
        private val playerBossbarMap = HashMap<UUID, MutableList<TopUI>>()

        operator fun get(uniqueId: UUID) = bossbarMap[uniqueId]

        @JvmStatic
        val Player.bossBars: List<TopUI>
            get() = mutableBossbars

        @JvmStatic
        fun Player.clearBossBars(){
            //visible한 애들에 한해 모든 보스바 REMOVE 패킷 보내기
            bossbarMap.values.filter { it.isVisible }.forEach {
                val packet = PacketContainer(Play.Server.BOSS)
                packet.uuiDs.write(0, it.uniqueId)
                packet.integers.write(0, 1)
                ProtocolLibrary.getProtocolManager().sendServerPacket(this, packet)
            }
        }

        private val Player.mutableBossbars: MutableList<TopUI>
            get() = playerBossbarMap.computeIfAbsent(uniqueId){ ArrayList(10) }

        private val adventureBossBarColorByBukkit = mapOf(
            BarColor.PINK to BossBar.Color.PINK,
            BarColor.BLUE to BossBar.Color.BLUE,
            BarColor.RED to BossBar.Color.RED,
            BarColor.GREEN to BossBar.Color.GREEN,
            BarColor.YELLOW to BossBar.Color.YELLOW,
            BarColor.PURPLE to BossBar.Color.PURPLE,
            BarColor.WHITE to BossBar.Color.WHITE,
        )
        private val bukkitBossBarColorByAdventure = adventureBossBarColorByBukkit.map { (bukkit, adventure) -> adventure to bukkit }.toMap()
        internal fun BarColor.toAdventure(): BossBar.Color = adventureBossBarColorByBukkit[this]!!
        internal fun BossBar.Color.toBukkit(): BarColor = bukkitBossBarColorByAdventure[this]!!
        private val nmsBossBarColorByAdventure = mapOf(
            BossBar.Color.PINK to BossEvent.BossBarColor.PINK,
            BossBar.Color.BLUE to BossEvent.BossBarColor.BLUE,
            BossBar.Color.RED to BossEvent.BossBarColor.RED,
            BossBar.Color.GREEN to BossEvent.BossBarColor.GREEN,
            BossBar.Color.YELLOW to BossEvent.BossBarColor.YELLOW,
            BossBar.Color.PURPLE to BossEvent.BossBarColor.PURPLE,
            BossBar.Color.WHITE to BossEvent.BossBarColor.WHITE,
        )
        internal fun BossBar.Color.toNMS(): BossEvent.BossBarColor = nmsBossBarColorByAdventure[this]!!


        private val adventureBossBarOverlayByBukkit = mapOf(
            BarStyle.SEGMENTED_6 to BossBar.Overlay.NOTCHED_6,
            BarStyle.SEGMENTED_10 to BossBar.Overlay.NOTCHED_10,
            BarStyle.SEGMENTED_12 to BossBar.Overlay.NOTCHED_12,
            BarStyle.SEGMENTED_20 to BossBar.Overlay.NOTCHED_20,
            BarStyle.SOLID to BossBar.Overlay.PROGRESS,
        )
        private val bukkitBossBarOverlayByAdventure = adventureBossBarOverlayByBukkit.map { (bukkit, adventure) -> adventure to bukkit }.toMap()
        internal fun BarStyle.toAdventure(): BossBar.Overlay = adventureBossBarOverlayByBukkit[this]!!
        internal fun BossBar.Overlay.toBukkit(): BarStyle = bukkitBossBarOverlayByAdventure[this]!!
        private val nmsBossBarOverlayByAdventure = mapOf(
            BossBar.Overlay.NOTCHED_6 to BossEvent.BossBarOverlay.NOTCHED_6,
            BossBar.Overlay.NOTCHED_10 to BossEvent.BossBarOverlay.NOTCHED_10,
            BossBar.Overlay.NOTCHED_12 to BossEvent.BossBarOverlay.NOTCHED_12,
            BossBar.Overlay.NOTCHED_20 to BossEvent.BossBarOverlay.NOTCHED_20,
            BossBar.Overlay.PROGRESS to BossEvent.BossBarOverlay.PROGRESS,
        )
        internal fun BossBar.Overlay.toNMS(): BossEvent.BossBarOverlay = nmsBossBarOverlayByAdventure[this]!!

        private val adventureBossBarFlagByBukkit = mapOf(
            BarFlag.CREATE_FOG to BossBar.Flag.CREATE_WORLD_FOG,
            BarFlag.PLAY_BOSS_MUSIC to BossBar.Flag.PLAY_BOSS_MUSIC,
            BarFlag.DARKEN_SKY to BossBar.Flag.DARKEN_SCREEN,
        )
        private val bukkitBossBarFlagByAdventure = adventureBossBarFlagByBukkit.map { (bukkit, adventure) -> adventure to bukkit }.toMap()
        internal fun BarFlag.toAdventure(): BossBar.Flag = adventureBossBarFlagByBukkit[this]!!

    }

}

