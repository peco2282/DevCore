package com.peco2282.devcore.entity.v1_21_4

import com.destroystokyo.paper.entity.ai.MobGoalHelper
import com.peco2282.devcore.entity.GoalDefinition
import com.peco2282.devcore.entity.GoalDefinitions
import com.peco2282.devcore.entity.InternalManager
import net.minecraft.world.entity.ai.goal.Goal
import org.bukkit.craftbukkit.entity.CraftMob
import org.bukkit.entity.Mob
import java.util.*


class NmsGoalAdapter(private val definition: GoalDefinition) : Goal() {
  init {
    val nmsFlags = EnumSet.noneOf(Flag::class.java)
    definition.getFlags().forEach {
      nmsFlags.add(MobGoalHelper.paperToVanilla(it))
    }
    setFlags(nmsFlags)
  }

  override fun canUse(): Boolean = definition.canUse()
  override fun canContinueToUse(): Boolean = definition.canContinueToUse()
  override fun isInterruptable(): Boolean = definition.isInterruptable()
  override fun start() = definition.start()
  override fun stop() = definition.stop()
  override fun tick() = definition.tick()
  override fun requiresUpdateEveryTick(): Boolean = definition.requiresUpdateEveryTick()

  override fun toString(): String = definition.name.ifEmpty { super.toString() }
}

class Manager : InternalManager {
  override fun applyGoal(
    mob: Mob,
    goal: GoalDefinitions.() -> Unit
  ) {
    if (mob !is CraftMob) throw IllegalArgumentException("Class does not extend CraftMob")
    val handle = mob.handle
    val g = GoalDefinitions().apply(goal)
    g.get().forEach {
      handle.goalSelector.addGoal(
        it.priority,
        NmsGoalAdapter(it)
      )
    }
  }
}
