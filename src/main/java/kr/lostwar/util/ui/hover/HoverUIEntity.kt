package kr.lostwar.util.ui.hover

import kr.lostwar.util.math.lerp
import kr.lostwar.util.nms.FakeArmorStand
import kr.lostwar.util.ui.text.StringUtil.mapColored
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

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