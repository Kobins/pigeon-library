package kr.lostwar.util.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class ItemData(
    val type: Material,
    val data: Int = 0
){

    val material: Material get() = type

    constructor(type: Material) : this(type, 0)
    constructor(itemStack: ItemStack) : this(itemStack.type, itemStack.itemMeta?.customModelData ?: 0)

    fun applied(itemStack: ItemStack): ItemStack {
        val item = itemStack.clone()
        item.type = type
        val meta = item.itemMeta
        meta.setCustomModelData(data)
        item.itemMeta = meta
        return item
    }
    fun apply(itemStack: ItemStack): ItemStack {
        itemStack.type = type
        val meta = itemStack.itemMeta
        meta.setCustomModelData(data)
        itemStack.itemMeta = meta
        return itemStack
    }

    fun toItemStack(): ItemStack {
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