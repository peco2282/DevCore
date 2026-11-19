package com.peco2282.devcore.loot

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/** Result of inserting rolled loot into an inventory. */
data class LootDelivery(
  val rolled: List<ItemStack>,
  val leftovers: List<ItemStack>
) {
  /** Whether every rolled stack fit. */
  val allFit: Boolean get() = leftovers.isEmpty()
}

/** A deterministic, reusable loot definition parameterized by a caller context. */
class LootTable<C> internal constructor(
  private val rolls: IntRange,
  private val weighted: List<WeightedEntry<C>>,
  private val guaranteed: List<LootFactory>,
  private val bonuses: List<BonusEntry>
) {
  /** Rolls this table with [random] and [context]. */
  fun roll(random: Random = Random.Default, context: C): List<ItemStack> = buildList {
    guaranteed.forEach { add(it.create(random)) }
    val eligible = weighted.filter { it.condition(context) }
    if (eligible.isNotEmpty()) {
      val selector = WeightedTable(eligible.map { it to it.weight })
      repeat(random.nextInt(rolls.first, rolls.last + 1)) {
        add(selector.pick(random).factory.create(random))
      }
    }
    bonuses.forEach { bonus ->
      if (random.nextDouble() < bonus.chance) add(bonus.factory.create(random))
    }
  }

  /** Rolls and inserts items, returning stacks that did not fit. */
  fun rollTo(inventory: Inventory, random: Random = Random.Default, context: C): LootDelivery {
    val rolled = roll(random, context)
    val leftovers = rolled.flatMap { inventory.addItem(it).values }
    return LootDelivery(rolled, leftovers)
  }
}

/** Rolls a context-free table. */
fun LootTable<Unit>.roll(random: Random = Random.Default): List<ItemStack> = roll(random, Unit)

/** Rolls a context-free table into [inventory]. */
fun LootTable<Unit>.rollTo(inventory: Inventory, random: Random = Random.Default): LootDelivery =
  rollTo(inventory, random, Unit)

/** Builds a [LootTable]. */
fun <C> lootTable(block: LootTableBuilder<C>.() -> Unit): LootTable<C> =
  LootTableBuilder<C>().apply(block).build()

/** DSL builder for [LootTable]. */
class LootTableBuilder<C> {
  var rolls: IntRange = 1..1
    set(value) {
      require(value.first > 0 && value.last >= value.first) { "Rolls must be a non-empty positive range" }
      field = value
    }

  private val weighted = mutableListOf<WeightedEntry<C>>()
  private val guaranteed = mutableListOf<LootFactory>()
  private val bonuses = mutableListOf<BonusEntry>()

  fun weight(weight: Int, condition: (C) -> Boolean = { true }, block: LootEntryBuilder.() -> Unit) {
    require(weight > 0) { "Weight must be positive" }
    weighted += WeightedEntry(weight, condition, LootEntryBuilder().apply(block).factory())
  }

  fun guaranteed(block: LootEntryBuilder.() -> Unit) {
    guaranteed += LootEntryBuilder().apply(block).factory()
  }

  fun bonus(chance: Double, block: LootEntryBuilder.() -> Unit) {
    require(chance in 0.0..1.0) { "Bonus chance must be between 0 and 1" }
    bonuses += BonusEntry(chance, LootEntryBuilder().apply(block).factory())
  }

  internal fun build(): LootTable<C> {
    require(rolls.first > 0 && rolls.last >= rolls.first) { "Rolls must be a non-empty positive range" }
    return LootTable(rolls, weighted, guaranteed, bonuses)
  }
}

/** Builds exactly one stack for a loot entry. */
class LootEntryBuilder {
  private var factory: LootFactory? = null

  fun item(material: Material, amount: IntRange = 1..1) {
    require(!material.isAir) { "Loot material must not be air" }
    require(amount.first > 0 && amount.last >= amount.first) { "Amount must be a positive range" }
    setFactory { random -> ItemStack(material, random.nextInt(amount.first, amount.last + 1)) }
  }

  fun item(stack: ItemStack, amount: IntRange = stack.amount..stack.amount) {
    require(!stack.type.isAir) { "Loot stack must not be air" }
    require(amount.first > 0 && amount.last >= amount.first) { "Amount must be a positive range" }
    setFactory { random -> stack.clone().also { it.amount = random.nextInt(amount.first, amount.last + 1) } }
  }

  private fun setFactory(create: (Random) -> ItemStack) {
    check(factory == null) { "A loot entry must define exactly one item" }
    factory = LootFactory(create)
  }

  internal fun factory(): LootFactory = requireNotNull(factory) { "A loot entry must define an item" }
}

internal fun interface LootFactory { fun create(random: Random): ItemStack }
internal data class WeightedEntry<C>(val weight: Int, val condition: (C) -> Boolean, val factory: LootFactory)
internal data class BonusEntry(val chance: Double, val factory: LootFactory)
