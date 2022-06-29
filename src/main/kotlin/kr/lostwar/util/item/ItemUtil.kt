package kr.lostwar.util.item

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta

object ItemUtil {
    fun Inventory.count(material: Material)
            = all(material).values
        .stream()
        .filter { it != null && it.type == material }
        .mapToInt { it.amount }
        .reduce(0, Integer::sum)

    fun <T : ItemStack> T.applyItemMeta(applier: ItemMeta.() -> Unit) = applyMeta(applier)
    fun <T : ItemStack, M : ItemMeta> T.applyMeta(applier: M.() -> Unit): T {
        val meta = itemMeta as? M ?: kotlin.run {
            ClassCastException("cannot cast ItemMeta").printStackTrace()
            return this
        }
        applier.invoke(meta)
        itemMeta = meta
        return this
    }

    fun Iterable<ItemStack?>.isEmptyContents(): Boolean {
        for(item in this){
            if(item != null && item.type != Material.AIR) return false
        }
        return true
    }

    fun Iterable<ItemStack?>.hasEmptySlot(): Boolean {
        for (item in this) {
            if (item == null || item.type == Material.AIR) return true
        }
        return false
    }

    fun Inventory.giveItem(item: ItemStack){
        val lefts = addItem(item).values
        location?.let { loc ->
            for(left in lefts){
                loc.world.dropItem(loc, left)
            }
        }
    }

    fun ItemStack?.isEnchantedBook(): Boolean {
        return if(this == null) false
        else hasItemMeta() && itemMeta is EnchantmentStorageMeta
    }

    fun ItemStack?.hasEnchants(): Boolean {
        return if(this == null) false
        else hasItemMeta() && itemMeta.hasEnchants()
    }

    fun ItemStack?.isEnchantCostable(): Boolean {
        return if(this == null) false
        else hasEnchants() || isEnchantedBook()
    }

    fun Inventory.materialCount(material: Material): Int {
        // what
        return storageContents!!.filterNotNull().filter { it.type == material }.sumOf { it.amount }
    }

    fun Inventory.removeMaterial(material: Material, amount: Int) {
        if (amount <= 0) return
        var leftAmount = amount
        for (slot in 0 until size) {
            val item: ItemStack = getItem(slot) ?: continue
            if (material == item.type) {
                val newAmount = item.amount - leftAmount
                if (newAmount > 0) {
                    item.amount = newAmount
                    break
                } else {
                    clear(slot)
                    leftAmount = -newAmount
                    if (leftAmount <= 0) break
                }
            }
        }
    }


}

