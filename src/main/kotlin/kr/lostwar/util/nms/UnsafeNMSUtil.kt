package kr.lostwar.util.nms

import kr.lostwar.util.ui.text.consoleWarn
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftArmorStand
import org.bukkit.entity.Entity
import java.util.function.BiFunction

/**
 * 이 클래스 안의 내용들은 '수정된 페이퍼'에 한해서만 동작합니다.
 */
object UnsafeNMSUtil {

    private val collidePredicateField =
        ReflectionUtil.getField(net.minecraft.world.entity.decoration.ArmorStand::class.java, "collidePredicate")

    fun org.bukkit.entity.ArmorStand.setCollidePredicate(predicate: BiFunction<Entity, Entity, Boolean>) {
        val nmsArmorStand = (this as? CraftArmorStand)?.handle
        if(nmsArmorStand == null){
            consoleWarn("tried get NMS ArmorStand, but it was not instance of CraftArmorStand on setCollidePredicate()")
            return
        }
        collidePredicateField.set(nmsArmorStand, predicate)
    }

}