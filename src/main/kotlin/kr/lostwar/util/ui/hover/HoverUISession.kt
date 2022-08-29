package kr.lostwar.util.ui.hover

import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.math.VectorUtil.minus
import kr.lostwar.util.math.VectorUtil.normalized
import kr.lostwar.util.math.VectorUtil.unaryMinus
import kr.lostwar.util.ui.hover.HoverUI.Companion.hoverUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

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
                        (it.value.targetLocation - player.eyeLocation).toVector()
                            .normalize()
                            .multiply(radius)
                    )
                }
                .filter { it.second <= detectRangeSqaured }
                .minByOrNull { it.second }
                ?.first
        }

        for((_, entity) in entityMap){
            val isHovered = currentHoveredEntity == entity
            val distance = player.eyeLocation.distance(entity.targetLocation)
            val radius = if(isHovered) hoveredRadius else radius
            // 플레이어 머리 위치 -> 실제 목표 위치
            val direction = (entity.targetLocation - player.eyeLocation).toVector().normalized
            val location = if(distance < radius) {
                entity.targetLocation
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

    operator fun get(key: String): HoverUIEntity {
        return entityMap.computeIfAbsent(key) {
            HoverUIEntity(
                player,
                key,
                player.eyeLocation,
                player.location,
                ItemStack(Material.AIR)
            ) { _, _, _ -> return@HoverUIEntity }
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
        entityMap.entries.forEach { (_, entity) ->
            entity.destroy()
        }
        entityMap.clear()
    }
}