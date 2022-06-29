package kr.lostwar.util.item

import kr.lostwar.util.item.ItemUtil.applyItemMeta
import kr.lostwar.util.item.ItemUtil.applyMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.Material.PLAYER_WALL_HEAD
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemFlag.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ItemBuilder : ItemStack {
    constructor(stack: ItemStack) : super(stack){
        addItemFlagAll()
    }

    @JvmOverloads
    constructor(mat: Material, data: Int = 0, amount: Int = 1) : super(mat, amount) {
        data(data)
        addItemFlagAll()
    }

    @JvmOverloads
    constructor(itemData: ItemData, amount: Int = 1) : super(itemData.material, amount) {
        data(itemData.data)
        addItemFlagAll()
    }

    fun material(mat: Material): ItemBuilder {
        type = mat
        return this
    }

    fun data(data: Int) = applyItemMeta {
        setCustomModelData(data)
    }

    fun materialAndData(data: ItemData) = applyItemMeta {
        type = data.material
        setCustomModelData(data.data)
    }

    fun displayName(displayName: Component) = applyItemMeta {
        displayName(displayName)
    }

    fun amount(amount: Int) = apply {
        this.amount = amount
    }
    fun setLore(vararg lore: Component) = setLore(lore.toList())
    fun setLore(lore: List<Component>) = applyItemMeta {
        lore(lore)
    }
    fun addLore(vararg lore: Component) = addLore(lore.toList())
    fun addLore(lore: List<Component>) = applyItemMeta {
        val originalLore = lore() ?: ArrayList()
        originalLore.addAll(lore)
        lore(originalLore)
    }

    fun setEnchant(glowing: Boolean): ItemBuilder {
        return applyItemMeta {
            if (glowing) {
                addItemFlags(HIDE_ENCHANTS)
                addEnchant(Enchantment.DIG_SPEED, 1, false)
            }
        }
    }

    fun head(p: Player?) = applyMeta<ItemBuilder, SkullMeta> {
        if (type != PLAYER_HEAD && type != PLAYER_WALL_HEAD) return@applyMeta
        owningPlayer = p
    }

    fun unbreakable(unbreakable: Boolean): ItemBuilder {
        return applyItemMeta {
            isUnbreakable = unbreakable
            if (unbreakable) {
                addItemFlags(HIDE_UNBREAKABLE)
                addItemFlags(HIDE_ATTRIBUTES)
                addItemFlags(HIDE_DESTROYS)
                addItemFlags(HIDE_PLACED_ON)
            }
        }
    }

    fun offhandAttackSpeed(speed: Int): ItemBuilder {
        val modifier = AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", speed.toDouble(), MULTIPLY_SCALAR_1)
        val im = itemMeta
        im.addAttributeModifier(GENERIC_ATTACK_SPEED, modifier)
        itemMeta = im
        return this
    }

    fun <T, Z> persistent(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z): ItemBuilder {
        applyItemMeta {
            persistentDataContainer.set(key, type, value)
        }
        return this
    }

    fun build(): ItemStack {
        return this
    }

    fun addItemFlagAll() = addItemFlag(*ItemFlag.values())
    fun addItemFlag(vararg flags: ItemFlag) = apply {
        addItemFlags(*flags)
    }
    fun removeItemFlag(vararg flags: ItemFlag) = apply {
        removeItemFlags(*flags)
    }

    @Deprecated("legacy string", ReplaceWith("displayName"))
    fun displayNameString(displayName: String?) = applyItemMeta {
        setDisplayName(displayName)
    }

    @Deprecated("legacy string", ReplaceWith("addLore"))
    fun addLoreString(vararg lore: String?): ItemBuilder {
        return addLoreString(lore.filterNotNull().toList())
    }
    @Deprecated("legacy string", ReplaceWith("addLore"))
    fun addLoreString(lore: List<String>): ItemBuilder {
        var itemLore = getLore()
        if (itemLore == null) {
            itemLore = ArrayList()
        }
        itemLore.addAll(lore)
        setLore(itemLore)
        return this
    }

    @Deprecated("legacy string", ReplaceWith("lore"))
    fun loreString(vararg lore: String?): ItemBuilder {
        setLore(lore.toList())
        return this
    }

    @Deprecated("legacy string", ReplaceWith("lore"))
    fun loreString(lore: List<String?>?): ItemBuilder {
        setLore(lore)
        return this
    }

    override fun equals(other: Any?): Boolean {
        if(other is ItemStack){
            if(type != other.type) return false
            if(amount != other.amount) return false
            val thisItemMeta = itemMeta
            val otherItemMeta = other.itemMeta
            if(thisItemMeta == null && otherItemMeta == null){
                return true
            }
            if((thisItemMeta == null) != (otherItemMeta == null)){
                return false
            }
            val customModelData = thisItemMeta.hasCustomModelData() && otherItemMeta.hasCustomModelData() && thisItemMeta.customModelData == otherItemMeta.customModelData
                    || !thisItemMeta.hasCustomModelData() && !otherItemMeta.hasCustomModelData()
            val displayName = !thisItemMeta.hasDisplayName() && !otherItemMeta.hasDisplayName()
                    || thisItemMeta.displayName() == otherItemMeta.displayName()
            return customModelData && displayName
        }
        return super.equals(other)
    }

    companion object {
        @JvmStatic
        fun ItemStack.toBuilder() = ItemBuilder(this)
    }
}