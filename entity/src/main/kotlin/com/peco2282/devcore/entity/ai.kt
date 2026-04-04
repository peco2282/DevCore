package com.peco2282.devcore.entity

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Mob
import org.bukkit.entity.Player

/**
 * Gets or sets whether this mob has AI.
 * This is a wrapper for [Mob.hasAI].
 */
var Mob.isNoAi: Boolean
  get() = !hasAI()
  set(value) {
    setAI(!value)
  }

/**
 * Targets the nearest player within the specified [radius].
 *
 * @param radius the maximum distance to search for a player
 * @param filter an optional filter to apply to candidates
 * @return the targeted player, or null if none was found
 */
fun Mob.targetNearestPlayer(radius: Double = 10.0, filter: (Player) -> Boolean = { true }): Player? {
  val nearest = world.players
    .filter { it.location.distanceSquared(location) <= radius * radius }
    .filter(filter)
    .minByOrNull { it.location.distanceSquared(location) }

  if (nearest != null) {
    target = nearest
  }
  return nearest
}

/**
 * Clears the current target and pathfinding of this mob.
 */
fun <M : Mob> M.clearAI() {
  target = null
  pathfinder.stopPathfinding()
}

/**
 * Makes this mob move to a specific location.
 *
 * @param location the location to move to
 * @param speed the movement speed
 * @return true if the pathfinding was successfully started
 */
fun Mob.moveTo(location: Location, speed: Double = 1.0): Boolean = pathfinder.moveTo(location, speed)


/**
 * Removes all goals from this mob.
 */
fun <M : Mob> M.clearGoals(): Unit = Bukkit.getMobGoals().removeAllGoals(this)

/**
 * Removes a goal from this mob by its [GoalKey].
 *
 * @param key the key of the goal to remove
 */
fun <M : Mob> M.removeGoal(key: GoalKey<M>): Unit = Bukkit.getMobGoals().removeGoal(this, key)

/**
 * Removes a specific goal instance from this mob.
 *
 * @param goal the goal instance to remove
 */
fun <M : Mob> M.removeGoal(goal: Goal<M>): Unit = Bukkit.getMobGoals().removeGoal(this, goal)

/**
 * Checks if this mob has a goal with the specified [GoalKey].
 *
 * @param key the key of the goal to check
 * @return true if the goal exists, false otherwise
 */
fun <M : Mob> M.hasGoal(key: GoalKey<M>): Boolean = Bukkit.getMobGoals().hasGoal(this, key)

/**
 * Checks if this mob has a goal with the specified [NamespacedKey].
 *
 * @param key the namespaced key of the goal to check
 * @return true if the goal exists, false otherwise
 */
inline fun <reified M : Mob> M.hasGoal(key: NamespacedKey): Boolean = Bukkit.getMobGoals().hasGoal(this, GoalKey.of(M::class.java, key))

/**
 * Adds a single goal to this mob with the specified priority.
 *
 * @param priority the priority of the goal (lower values execute first)
 * @param goal the goal to add
 */
fun <M : Mob> M.addGoals(priority: Int, goal: Goal<M>) = Bukkit.getMobGoals().addGoal(this, priority, goal)

/**
 * Adds multiple goals to this mob with the same priority.
 *
 * @param priority the priority of the goals (lower values execute first)
 * @param goals the goals to add
 */
fun <M : Mob> M.addGoals(priority: Int, vararg goals: Goal<M>) = goals.forEach { addGoals(priority, it) }

/**
 * Retrieves all goals from this mob with the specified [GoalKey].
 *
 * @param key the key of the goals to retrieve
 * @return a collection of goals matching the key
 */
fun <M : Mob> M.getGoal(key: GoalKey<M>): Collection<Goal<M>> = Bukkit.getMobGoals().getGoals(this, key)

/**
 * Retrieves all goals from this mob with the specified [NamespacedKey].
 *
 * @param key the namespaced key of the goals to retrieve
 * @return a collection of goals matching the key
 */
inline fun <reified M : Mob> M.getGoal(key: NamespacedKey): Collection<Goal<M>> = Bukkit.getMobGoals().getGoals(this, GoalKey.of(M::class.java, key))

/**
 * Retrieves all currently active (running) goals of this mob.
 *
 * @return a collection of currently executing goals
 */
fun <M : Mob> M.getActiveGoals(): Collection<Goal<M>> = Bukkit.getMobGoals().getRunningGoals(this)

/**
 * Retrieves all goals registered to this mob, both active and inactive.
 *
 * @return a collection of all goals
 */
fun <M : Mob> M.getAllGoals(): Collection<Goal<M>> = Bukkit.getMobGoals().getAllGoals(this)
