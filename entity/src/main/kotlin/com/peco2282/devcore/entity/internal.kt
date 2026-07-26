package com.peco2282.devcore.entity

import org.bukkit.Bukkit
import org.bukkit.entity.Mob
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
interface InternalManager {
  fun applyGoal(mob: Mob, goal: GoalDefinitions.() -> Unit)
}

internal fun getInternalManager(): InternalManager {
  val version = Bukkit.getMinecraftVersion()
  val clsName = "com.peco2282.devcore.entity.v1_21_4.Manager"
  val cls = Class.forName(clsName)
  return cls.getConstructor().newInstance() as InternalManager
}

object Manager: InternalManager by getInternalManager()
