package com.peco2282.devcore.kit

import com.peco2282.devcore.cooldown.persistent.PersistentCooldowns
import com.peco2282.devcore.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.WeakHashMap
import kotlin.time.Duration

enum class KitFailure { UNKNOWN_KIT, NO_PERMISSION, COOLDOWN }
data class KitResult(val delivered: List<ItemStack> = emptyList(), val dropped: List<ItemStack> = emptyList(), val failure: KitFailure? = null) {
  val success get() = failure == null
}
data class KitDefinition(val name: String, val permission: String?, val cooldown: Duration, val items: List<ItemStack>)

class KitItemsBuilder {
  internal val items = mutableListOf<ItemStack>()
  fun item(material: Material, amount: Int = 1) { items += ItemStack(material, amount) }
  fun item(stack: ItemStack) { items += stack.clone() }
  fun custom(item: CustomItem, amount: Int = 1) { items += item.create(amount) }
}
class KitBuilder(private val name: String) {
  var permission: String? = null
  var cooldown: Duration = Duration.ZERO
  private val items = mutableListOf<ItemStack>()
  fun items(block: KitItemsBuilder.() -> Unit) { items += KitItemsBuilder().apply(block).items }
  internal fun build(): KitDefinition { require(items.isNotEmpty()); require(!cooldown.isNegative()); return KitDefinition(name, permission, cooldown, items.map(ItemStack::clone)) }
}

class KitRegistry internal constructor(plugin: Plugin) {
  private val definitions = linkedMapOf<String, KitDefinition>()
  private val cooldowns = PersistentCooldowns(plugin)
  fun define(name: String, block: KitBuilder.() -> Unit): KitDefinition {
    require(name.matches(Regex("[a-z0-9._-]+")))
    return KitBuilder(name).apply(block).build().also { definitions[name] = it }
  }
  fun saveFrom(inventory: Inventory, name: String): KitDefinition = define(name) {
    items { inventory.contents.filterNotNull().filterNot { it.type.isAir }.forEach(::item) }
  }
  fun get(name: String) = definitions[name]
  fun give(name: String, player: Player): KitResult {
    val kit = definitions[name] ?: return KitResult(failure = KitFailure.UNKNOWN_KIT)
    if (kit.permission?.let(player::hasPermission) == false) return KitResult(failure = KitFailure.NO_PERMISSION)
    if (!cooldowns.tryUse(player, "kit/$name", kit.cooldown)) return KitResult(failure = KitFailure.COOLDOWN)
    val delivered = kit.items.map(ItemStack::clone)
    val leftovers = delivered.flatMap { player.inventory.addItem(it).values }
    leftovers.forEach { player.world.dropItemNaturally(player.location, it) }
    return KitResult(delivered, leftovers)
  }
}

private val registries = WeakHashMap<Plugin, KitRegistry>()
fun Plugin.kits(): KitRegistry = synchronized(registries) { registries.getOrPut(this) { KitRegistry(this) } }
