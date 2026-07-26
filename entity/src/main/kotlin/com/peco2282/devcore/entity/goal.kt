package com.peco2282.devcore.entity

import com.destroystokyo.paper.entity.ai.GoalType
import org.bukkit.entity.Mob
import java.util.*

@EntityDsl
class GoalDefinitions {
  private val goals = mutableListOf<GoalDefinition>()
  private val creator = ::GoalDefinitionImpl

  fun get() = goals.toList()

  infix fun goal(goal: GoalDefinition.() -> Unit): GoalDefinition {
    val g = creator().apply(goal)
    goals.add(g)
    return g
  }
}

@EntityDsl
interface GoalDefinition {
  var priority: Int
  var name: String

  var canUse: Boolean

  fun canUse(block: () -> Boolean)
  fun canContinueToUse(block: () -> Boolean)
  fun isInterruptable(block: () -> Boolean)
  fun onStart(block: () -> Unit)
  fun onStop(block: () -> Unit)
  fun requiresUpdateEveryTick(block: () -> Boolean)
  fun onTick(block: () -> Unit)

  fun canUse(): Boolean
  fun canContinueToUse(): Boolean = this.canUse

  fun isInterruptable(): Boolean = true
  fun start()

  fun stop()
  fun requiresUpdateEveryTick(): Boolean = false

  fun tick()
  fun setFlags(flagSet: EnumSet<GoalType>)

  fun getFlags(): EnumSet<GoalType>

  fun hasFlag(flag: GoalType): Boolean

  fun addFlag(flag: GoalType)

  fun adjustedTickDelay(adjustment: Int): Int

  fun reducedTickDelay(reduction: Int): Int

}


internal class GoalDefinitionImpl : GoalDefinition {
  override var priority: Int = 0
  override var name: String = ""
  override var canUse: Boolean = true

  private var canUseFunc: () -> Boolean = { canUse }
  private var canContinueToUseFunc: () -> Boolean = { this.canUse }
  private var isInterruptableFunc: () -> Boolean = { true }
  private var startFunc: () -> Unit = {}
  private var stopFunc: () -> Unit = {}
  private var requiresUpdateEveryTickFunc: () -> Boolean = { false }
  private var tickFunc: () -> Unit = {}

  private val flags = EnumSet.noneOf(GoalType::class.java)

  override fun canUse(block: () -> Boolean) {
    canUseFunc = block
  }

  override fun canContinueToUse(block: () -> Boolean) {
    canContinueToUseFunc = block
  }

  override fun isInterruptable(block: () -> Boolean) {
    isInterruptableFunc = block
  }

  override fun onStart(block: () -> Unit) {
    startFunc = block
  }

  override fun onStop(block: () -> Unit) {
    stopFunc = block
  }

  override fun requiresUpdateEveryTick(block: () -> Boolean) {
    requiresUpdateEveryTickFunc = block
  }

  override fun onTick(block: () -> Unit) {
    tickFunc = block
  }

  override fun canUse(): Boolean = canUse && canUseFunc()
  override fun canContinueToUse(): Boolean = canContinueToUseFunc()
  override fun isInterruptable(): Boolean = isInterruptableFunc()
  override fun start() = startFunc()
  override fun stop() = stopFunc()
  override fun requiresUpdateEveryTick(): Boolean = requiresUpdateEveryTickFunc()
  override fun tick() = tickFunc()

  override fun setFlags(flagSet: EnumSet<GoalType>) {
    flags.clear()
    flags.addAll(flagSet)
  }

  override fun getFlags(): EnumSet<GoalType> = flags
  override fun hasFlag(flag: GoalType): Boolean = flags.contains(flag)
  override fun addFlag(flag: GoalType) {
    flags.add(flag)
  }

  override fun adjustedTickDelay(adjustment: Int): Int = adjustment
  override fun reducedTickDelay(reduction: Int): Int = reduction
}

fun goal(goal: GoalDefinitions.() -> Unit) = GoalDefinitions().apply(goal)

@JvmName("mobGoal")
fun Mob.goal(goal: GoalDefinitions.() -> Unit) {
  Manager.applyGoal(this, goal)
}
