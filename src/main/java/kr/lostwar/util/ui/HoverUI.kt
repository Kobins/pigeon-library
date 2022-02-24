package kr.lostwar.util.ui

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.*
import kr.lostwar.util.text.console
import kr.lostwar.util.ui.HoverUI.Companion.hoverUI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object HoverUIListener : PacketAdapter(
    PigeonLibraryPlugin.instance,
    PacketType.Play.Server.UNLOAD_CHUNK
), Listener {

    override fun onPacketReceiving(event: PacketEvent) {
    }

    override fun onPacketSending(event: PacketEvent) {
//        if(event.isPlayerTemporary) return
//        val player = event.player
//        val packet = event.packet
//        val chunkX = packet.integers.read(0)
//        val chunkZ = packet.integers.read(1)
//        event.isCancelled = HoverUI[player].onChunkUnload(chunkX, chunkZ)
//        if(event.isCancelled){
//            console("chunk unload cancelled by HoverUIListener($chunkX, $chunkZ)")
//        }
    }

    @EventHandler
    fun ChunkUnloadEvent.onUnload(){
        val chunkX = chunk.x
        val chunkZ = chunk.z
        Bukkit.getOnlinePlayers().forEach {
            if(it.hoverUI.onChunkUnload(chunkX, chunkZ)){
                console("&cchunk unload detected by HoverUIListener&e($chunkX, $chunkZ)")
            }
        }
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteract(){
        HoverUI[player].onClick(true)
    }
    @EventHandler
    fun PlayerInteractEntityEvent.onInteract(){
        HoverUI[player].onClick(true)
    }
    @EventHandler
    fun PlayerInteractEvent.onInteract(){
        val isRightClick = action == RIGHT_CLICK_AIR || action == RIGHT_CLICK_BLOCK
        HoverUI[player].onClick(isRightClick)
    }
}

class HoverUI private constructor(
    val player: Player,
){
    private val sessionsMap = HashMap<String, HoverUISession>()

    operator fun get(key: String): HoverUISession{
        return sessionsMap.computeIfAbsent(key) { HoverUISession(player, it) }
    }

    fun remove(key: String){
        val session = sessionsMap[key] ?: return
        session.destroy()
        sessionsMap.remove(key)
    }

    fun destroy(){
        sessionsMap.forEach { (key, session) ->
            session.destroy()
        }
        sessionsMap.clear()
        playerMap.remove(player.uniqueId)
    }

    fun onClick(isRightClick: Boolean){
        if(sessionsMap.isEmpty()) return
        sessionsMap.values.forEach { it.onClick(isRightClick) }
    }

    fun onChunkUnload(x: Int, z: Int): Boolean{
        return sessionsMap.values.any {
            it.onChunkUnload(x, z)
        }
    }


    companion object{
        private val playerMap = HashMap<UUID, HoverUI>()
        val Player.hoverUI: HoverUI
            get() = get(this)
        @JvmStatic
        operator fun get(player: Player): HoverUI{
            return playerMap[player.uniqueId]?.takeIf {
                val isPlayer = it.player == player
                if(!isPlayer){
                    it.destroy()
                }
                isPlayer
            } ?: run {
                val hoverUI = HoverUI(player)
                playerMap[player.uniqueId] = hoverUI
                hoverUI
            }
        }
    }
}

class HoverUISession(
    val player: Player,
    val key: String,
){
    var radius: Double = 2.0
    var hoveredRadius: Double = 1.5
    var detectRange: Double = 1.5
    var useMouseHover = true
    var onTick: (HoverUIEntity) -> Unit = {}

    private val sessionCreatedWorld = player.world
    private val entityMap = HashMap<String, HoverUIEntity>()

    private var task: BukkitTask? = object: BukkitRunnable(){
        override fun run() {
            tick()
        }
    }.runTaskTimerAsynchronously(PigeonLibraryPlugin.instance, 0, 1)


    private var currentHoveredEntity: HoverUIEntity? = null
        set(value) {
            val changed = field != value
            if(changed) {
                field?.isHovering = false
            }
            field = value
            if(changed){
                value?.isHovering = true
            }
        }
    private fun tick(){
        // 플레이어 오프라인 또는 월드 달라지면 이면 할당 해제
        if(!player.isOnline || player.world.name != sessionCreatedWorld.name){
            player.hoverUI.remove(key)
            return
        }
        // 엔티티 없으면 업데이트 안함
        if(entityMap.isEmpty()){
            return
        }
        if(useMouseHover) {
            val playerDirection = player.eyeLocation.direction
            val raycasted = playerDirection.normalize().multiply(radius)
            val detectRangeSqaured = detectRange * detectRange
            currentHoveredEntity = entityMap
                .map {
                    it.value to raycasted.distanceSquared(
                        // 목표를 구 좌표로 변환한 location과 raycasted 사이의 거리
                        (it.value.targettingLocation - player.eyeLocation).toVector()
                            .normalize()
                            .multiply(radius)
                    )
                }
                .filter { it.second <= detectRangeSqaured }
                .minByOrNull { it.second }
                ?.first
        }

        for((key, entity) in entityMap){
            val isHovered = currentHoveredEntity == entity
            val distance = player.eyeLocation.distance(entity.targettingLocation)
            val radius = if(isHovered) hoveredRadius else radius
            // 플레이어 머리 위치 -> 실제 목표 위치
            val direction = (entity.targettingLocation - player.eyeLocation).toVector().normalized
            val location = if(distance < radius) {
                entity.targettingLocation
            }else {
                direction.normalized
                    .multiply(radius)
                    .toLocation(player.world)
                    .add(player.eyeLocation)
            }
//            player.spawnParticle(Particle.CRIT, location, 1)
            // 머리 방향 설정
            location.direction = -direction
            entity.location = location
            entity.tick()
            onTick(entity)
        }
    }

    internal fun onClick(isRightClick: Boolean){
        val hovered = currentHoveredEntity ?: return
        hovered.onClick(this, hovered, isRightClick)
    }

    internal fun onChunkUnload(x: Int, z: Int): Boolean{
        return entityMap.values.filter {
            it.onChunkUnload(x, z)
        }.isNotEmpty()
    }

    operator fun get(key: String): HoverUIEntity{
        return entityMap.computeIfAbsent(key) {
            HoverUIEntity(
                player,
                key,
                player.eyeLocation,
                player.location,
                ItemStack(AIR)
            ){ _, _, _ -> return@HoverUIEntity }
        }
    }

    fun removeStartsWith(key: String){
        entityMap.keys.filter { it.startsWith(key) }.forEach {
            remove(it)
        }
    }
    fun remove(key: String){
        val entity = entityMap[key] ?: return
        if(currentHoveredEntity == entity){
            currentHoveredEntity = null
        }
        entity.destroy()
        entityMap.remove(key)
    }

    fun isEmpty() = entityMap.isEmpty()

    fun destroy(){
        if(task?.isCancelled == false)
            task?.cancel()
        task = null
        destroyEntities()
    }

    fun destroyEntities(){
        if(entityMap.isEmpty()) return
        entityMap.entries.forEach { (key, entity) ->
            entity.destroy()
        }
        entityMap.clear()
    }
}

class HoverUIEntity(
    val player: Player,
    val key: String,
    location: Location,
    var targettingLocation: Location, // just storing data
    item: ItemStack,
    var onClick: (HoverUISession, HoverUIEntity, Boolean) -> Unit,
){
    var isHovering = false
        set(value) {
            field = value
            displayName = if(value){
                hoveredDisplayName
            }else{
                normalDisplayName
            }
        }
    var normalDisplayName: List<String> = emptyList()
        set(value) {
            field = value
            if(!isHovering){
                displayName = value
            }
        }
    var hoveredDisplayName: List<String> = emptyList()
        set(value) {
            field = value
            if(isHovering){
                displayName = value
            }
        }
    val entity = FakeArmorStand(location)
    val dummyEntities = mutableListOf<FakeArmorStand>()

    init {
        entity.addObserver(player)
    }

    /**
     * 엔티티의 위치를 가져오거나 설정합니다.
     * yaw는 엔티티 방향으로, pitch는 headPose로 설정됩니다.
     */
    var location: Location = location
        set(value) {
            field = value
        }
    /**
     * 표시되는 이름을 설정합니다.
     */
    var displayName: List<String> = normalDisplayName
        set(raw) {
            val colored = raw.mapColored()
            fun fillStrings(){
                if(colored.isEmpty()){
                    entity.displayName = ""
                    return
                }
                entity.displayName = colored[0]
                for (i in 1 until colored.size) {
                    dummyEntities[i - 1].displayName = colored[i]
                }
            }
            val entitySize = dummyEntities.size
            val newStringsSize = max(0, colored.size - 1)
            val gap = abs(newStringsSize - entitySize)

            if(newStringsSize > entitySize){
                for(i in 0 until gap){
                    addEntity()
                }
            }else if(newStringsSize < entitySize){
                for(i in 0 until gap){
                    removeEntity()
                }
            }
            fillStrings()
            field = colored
        }

    private fun addEntity(){
        val entity = FakeArmorStand(location)
        dummyEntities.add(entity)
        entity.addObserver(player)
    }

    private fun removeEntity(){
        val removed = dummyEntities.removeLastOrNull() ?: return
        removed.destroy()
    }

    /**
     * 아이템을 설정합니다.
     */
    var item: ItemStack = item
        set(value) {
            field = value
            entity.head = value
        }

    fun destroy(){
        entity.destroy()
        dummyEntities.forEach { it.destroy() }
        dummyEntities.clear()
    }
    
    internal fun tick(){
        val oldLocation = entity.location
        val newLocation = location
        if(!player.isInsideVehicle && abs(oldLocation.y - newLocation.y) <= 2) { //2 이하면 느린 lerp (위아래 흔들림 제어)
            entity.location = Location(
                newLocation.world,
                newLocation.x,
                lerp(oldLocation.y, newLocation.y, 0.1),
                newLocation.z,
                newLocation.yaw,
                newLocation.pitch,
            )
            for((index, armorStand) in dummyEntities.withIndex()){
                val newLocationDummy = newLocation.clone().add(0.0, 0.25 * (index+1), 0.0)
                armorStand.location = Location(
                    newLocationDummy.world,
                    newLocationDummy.x,
                    lerp(armorStand.location.y, newLocationDummy.y, 0.1),
                    newLocationDummy.z,
                    newLocationDummy.yaw,
                    newLocationDummy.pitch,
                )
            }
        }else{ //2보다 크면 바로 set
            entity.location = newLocation
            for((index, armorStand) in dummyEntities.withIndex()){
                val newLocationDummy = newLocation.clone().add(0.0, 0.25 * (index+1), 0.0)
                armorStand.location = newLocationDummy
            }
        }

    }

    internal fun onChunkUnload(x: Int, z: Int): Boolean{
        val locX = location.x.roundToInt() shr 4
        val locZ = location.z.roundToInt() shr 4
        val isUnloadingSelf = locX == x && locZ == z
        if(isUnloadingSelf)
            entity.location = player.eyeLocation
        return isUnloadingSelf
    }
}