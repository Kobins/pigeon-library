package kr.lostwar.util

import kr.lostwar.util.text.colored
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta


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

fun Iterable<String>.mapColored(): List<String>{
    return map { it.colored() }
}

data class ItemData(
        val type: Material,
        val data: Int = 0
){

    val material: Material get() = type

    constructor(type: Material) : this(type, 0)
    constructor(itemStack: ItemStack) : this(itemStack.type, itemStack.itemMeta?.customModelData ?: 0)

    fun applied(itemStack: ItemStack): ItemStack{
        val item = itemStack.clone()
        item.type = type
        val meta = item.itemMeta
        meta.setCustomModelData(data)
        item.itemMeta = meta
        return item
    }
    fun apply(itemStack: ItemStack): ItemStack{
        itemStack.type = type
        val meta = itemStack.itemMeta
        meta.setCustomModelData(data)
        itemStack.itemMeta = meta
        return itemStack
    }

    fun toItemStack(): ItemStack{
        val itemStack = ItemStack(type)
        if(type == Material.AIR){
            return itemStack
        }
        itemStack.apply(this)
        return itemStack
    }

    fun clone() = ItemData(type, data)

    override fun hashCode(): Int {
        return material.hashCode() + data
    }

    override fun equals(other: Any?): Boolean {
        if(other is ItemStack){
            return equals(other)
        }
        return super.equals(other)
    }

    fun equals(itemStack: ItemStack): Boolean{
        val meta = itemStack.itemMeta
        val cmd = when{
            meta == null || !meta.hasCustomModelData() -> 0
            else -> meta.customModelData
        }
        return itemStack.type == type
                && cmd == data
    }

    override fun toString(): String {
        return "$type, $data"
    }

    companion object{
        @JvmStatic
        fun parse(string: String) = toItemData(string)
        @JvmStatic
        @JvmOverloads
        fun get(type: Material, data: Int = 0) = ItemData(type, data)

        @JvmStatic
        fun ItemStack.apply(itemData: ItemData) = itemData.apply(this)
        @JvmStatic
        fun ItemStack.applied(itemData: ItemData) = itemData.applied(this)

        @JvmStatic
        @JvmOverloads
        fun toItemData(string: String?, default: ItemData? = null): ItemData?{
            if(string == null){
                return default
            }
            val splitted = string.split(',').map { it.trim() }
            if(splitted.isEmpty()){
                return default
            }
            val type = Material.getMaterial(splitted[0]) ?: return default
            if(splitted.size == 1){
                return ItemData(type)
            }
            val data = splitted[1].toIntOrNull() ?: return default
            if(splitted.size == 2){
                return ItemData(type, data)
            }
            return default
        }
    }

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
    return storageContents.filterNotNull().filter { it.type == material }.sumOf { it.amount }
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
