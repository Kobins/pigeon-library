package kr.lostwar.util.ui.hover

import kr.lostwar.PigeonLibraryPlugin
import kr.lostwar.util.math.VectorUtil
import kr.lostwar.util.math.VectorUtil.minus
import kr.lostwar.util.math.VectorUtil.normalized
import kr.lostwar.util.math.VectorUtil.plus
import kr.lostwar.util.math.VectorUtil.times
import kr.lostwar.util.math.VectorUtil.unaryMinus
import kr.lostwar.util.math.toRadians
import kr.lostwar.util.ui.hover.HoverUI.Companion.hoverUI
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class HoverUISession(
    val player: Player,
    val key: String,
){
    var radius: Double = 2.0
    var hoveredRadius: Double = 1.5
    var detectRange: Double = 1.5
    var useMouseHover = true
    var onTick: (HoverUIEntity) -> Unit = {}
    var playerFOVInRadian = 50.0.toRadians()
        set(value) {
            field = value
            updateConstants()
        }
    private var playerFOVHalfInRadian = 0.0
    private var playerCosFOVHalf = 0.0
    private var playerSinFOVHalf = 0.0
    private fun updateConstants() {
        playerFOVHalfInRadian = playerFOVInRadian / 2.0
        playerCosFOVHalf = cos(playerFOVHalfInRadian)
        playerSinFOVHalf = sin(playerFOVHalfInRadian)
    }
    init {
        updateConstants()
    }

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
            val detectRangeSquared = detectRange * detectRange
            currentHoveredEntity = entityMap
                .map {
                    it.value to raycasted.distanceSquared(
                        // 목표를 구 좌표로 변환한 location과 raycasted 사이의 거리
                        (it.value.targetLocation - player.eyeLocation).toVector()
                            .normalize()
                            .multiply(radius)
                    )
                }
                .filter { it.second <= detectRangeSquared }
                .minByOrNull { it.second }
                ?.first
        }

        val eyeOrigin = player.eyeLocation
        // 왼손 좌표계
        val forward = eyeOrigin.direction
        val right = VectorUtil.UP.crossProduct(forward)
        val up = forward.getCrossProduct(right)

        for((_, entity) in entityMap){
            val isHovered = currentHoveredEntity == entity
            val target = entity.targetLocation
            if(player.world != target.world) continue

            val targetRay = target.toVector() - eyeOrigin
            val direction = targetRay.normalized
            val distance = targetRay.length()
            val radius = if(isHovered) hoveredRadius else radius
            val angleByDot = forward.dot(direction)
            val location: Location
            val hoverDirection: Vector
            val length = min(distance, radius)
            // 시야각 바깥인 경우
            if(angleByDot < playerCosFOVHalf) {
                val fPrimeLength = playerCosFOVHalf * length
                val fPrime = forward * fPrimeLength
                val u = targetRay - fPrime
                val uPrime = Vector(right.dot(u), up.dot(u), 0.0).normalize()
                entity.relativeDirection = uPrime.clone()
                // distance, radius 중 최솟값 반지름만큼 곱함
                uPrime.multiply(playerSinFOVHalf * length)
                val result = up * uPrime.y + right * uPrime.x
                val finalDirection = fPrime.add(result)
                location = eyeOrigin + finalDirection
                hoverDirection = finalDirection.normalized
            }
            // 시야각 안인 경우
            else{
                entity.relativeDirection = null
                hoverDirection = direction
                // 거리 내면 그대로, 거리 밖이면 최대거리로 보정
                location = if(distance < radius) {
                    target
                }else {
                    direction.normalized
                        .multiply(radius)
                        .toLocation(player.world)
                        .add(eyeOrigin)
                }
            }
//            player.spawnParticle(Particle.CRIT, location, 1)
            val finalLocation: Location
            val result = player.world.rayTraceBlocks(eyeOrigin, hoverDirection, length, FluidCollisionMode.ALWAYS, true)
            if(result == null) {
                finalLocation = location
            }else{
                finalLocation = result.hitPosition.toLocation(player.world)
            }
            // 머리 방향 설정
            finalLocation.direction = -direction
            entity.location = finalLocation
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