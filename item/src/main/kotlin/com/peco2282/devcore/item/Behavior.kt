package com.peco2282.devcore.item

import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

/** What a custom item does. Built through [CustomItemBuilder]. */
internal class Behavior(
  val onRightClick: List<(PlayerInteractEvent) -> Unit>,
  val onLeftClick: List<(PlayerInteractEvent) -> Unit>,
  val onAttack: List<(EntityDamageByEntityEvent) -> Unit>,
  val onBreak: List<(BlockBreakEvent) -> Unit>,
  val onPlace: List<(BlockPlaceEvent) -> Unit>,
  val onConsume: List<(PlayerItemConsumeEvent) -> Unit>,
  val onDrop: List<(PlayerDropItemEvent) -> Unit>,
  val placeable: Boolean
)
