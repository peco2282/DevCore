package com.peco2282.devcore.entity

import com.peco2282.devcore.util.versioned
import org.bukkit.entity.Mob
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
interface InternalManager {
  fun applyGoal(mob: Mob, goal: GoalDefinitions.() -> Unit)
}

/**
 * Per-version NMS implementations of [InternalManager]. Add a `v1_XX_X` submodule and a
 * `since` line here to support another Minecraft version.
 */
private val managers = versioned<InternalManager>("Entity AI goals") {
  since("1.21.4", "com.peco2282.devcore.entity.v1_21_4.Manager")
}

internal fun getInternalManager(): InternalManager = managers.load()

object Manager: InternalManager by getInternalManager()
