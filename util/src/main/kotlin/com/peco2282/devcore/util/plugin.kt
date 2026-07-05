package com.peco2282.devcore.util

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * Registers an event listener for a specific event type using a simplified syntax.
 * 
 * This extension function provides a convenient way to register event handlers without
 * needing to create separate Listener classes. It leverages Kotlin's reified type parameters
 * to automatically determine the event type at compile time, eliminating the need for
 * explicit class references.
 * 
 * The function registers the event with NORMAL priority and does not ignore cancelled events.
 * The handler is executed on the main server thread when the specified event is fired.
 * 
 * ## Usage Example
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     override fun onEnable() {
 *         // Register a player join event handler
 *         on<PlayerJoinEvent> { event ->
 *             event.player.sendMessage("Welcome to the server!")
 *         }
 *         
 *         // Register a block break event handler
 *         on<BlockBreakEvent> { event ->
 *             if (event.block.type == Material.DIAMOND_ORE) {
 *                 event.player.sendMessage("You found diamonds!")
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * ## How It Works
 * - Uses Bukkit's PluginManager to register a dynamic event listener
 * - Creates an anonymous Listener object for registration purposes
 * - Checks event type at runtime and invokes the handler only for matching events
 * - Automatically handles type casting safely
 * 
 * ## Important Notes
 * - The event handler runs synchronously on the main server thread
 * - Events are registered with EventPriority.NORMAL
 * - Cancelled events are NOT ignored (ignoreCancelled = false)
 * - The handler lambda receives the event instance as a parameter
 * - Type safety is enforced at compile time through reified generics
 * 
 * @param T The type of event to listen for (must extend org.bukkit.event.Event)
 * @param handler A lambda function that receives the event and processes it.
 *                This function is called each time the event is fired.
 * @receiver Plugin The plugin instance that will own this event registration.
 *                  The listener will be automatically unregistered when the plugin is disabled.
 * 
 * @see org.bukkit.event.Event
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.EventPriority
 * @see org.bukkit.plugin.PluginManager.registerEvent
 */
inline fun <reified T : Event> Plugin.on(
  noinline handler: (T) -> Unit
) {
  Bukkit.getPluginManager().registerEvent(
    T::class.java,
    object : Listener {},
    EventPriority.NORMAL,
    { _, event ->
      if (event is T) {
        handler(event)
      }
    },
    this,
    false
  )
}

/**
 * Schedules a task to run after a specified delay on the main server thread.
 * 
 * This extension function provides a simplified way to schedule delayed tasks without
 * manually creating BukkitRunnable instances. The delay is specified in seconds and
 * automatically converted to Minecraft ticks (20 ticks = 1 second).
 * 
 * The task executes exactly once after the delay period has elapsed. All code within
 * the task lambda runs synchronously on the main server thread, making it safe to
 * interact with the Bukkit API and modify game state.
 * 
 * ## Usage Example
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     override fun onEnable() {
 *         // Send a message after 5 seconds
 *         runTaskLater(5.0) {
 *             server.broadcastMessage("5 seconds have passed!")
 *         }
 *         
 *         // Teleport player after 3.5 seconds with cancellation support
 *         val task = runTaskLater(3.5) {
 *             player.teleport(spawnLocation)
 *             player.sendMessage("Teleported!")
 *         }
 *         
 *         // Can cancel the task before it runs
 *         // task.cancel()
 *     }
 * }
 * ```
 * 
 * ## Tick Conversion
 * The delay parameter is in seconds and converted to ticks using the formula:
 * - Ticks = seconds × 20
 * - Example: 1.0 second = 20 ticks
 * - Example: 0.5 seconds = 10 ticks
 * - Example: 2.5 seconds = 50 ticks
 * 
 * ## Important Notes
 * - The task runs synchronously on the main server thread
 * - Minimum practical delay is 0.05 seconds (1 tick)
 * - Fractional ticks are truncated (e.g., 1.51 seconds = 30 ticks)
 * - The task has access to BukkitRunnable methods (e.g., cancel(), isCancelled())
 * - If the plugin is disabled before the task runs, the task is automatically cancelled
 * - For repeating tasks, use runTaskTimer() instead
 * 
 * ## Thread Safety
 * Since this runs on the main thread, it's safe to:
 * - Modify blocks, entities, and inventory
 * - Send messages to players
 * - Interact with any Bukkit/Paper API
 * 
 * For asynchronous operations, consider using Bukkit's async scheduler methods instead.
 * 
 * @param delay The delay before execution in seconds. Must be non-negative.
 *              Converted to ticks by multiplying by 20.
 * @param task A lambda function containing the code to execute after the delay.
 *             Receives BukkitRunnable as receiver, allowing access to scheduling methods.
 * @receiver Plugin The plugin that owns this scheduled task. Used for lifecycle management.
 * @return BukkitTask A task object that can be used to cancel the scheduled execution
 *                    or check its status.
 * 
 * @see BukkitRunnable
 * @see BukkitTask
 * @see org.bukkit.scheduler.BukkitScheduler.runTaskLater
 */
inline fun Plugin.runTaskLater(
  delay: Double,
  crossinline task: BukkitRunnable.() -> Unit
): BukkitTask {
  val runnable = object : BukkitRunnable() {
    override fun run() = this.task()
  }
  return runnable.runTaskLater(this, (delay * 20).toLong())
}

/**
 * Schedules a repeating task to run at fixed intervals on the main server thread.
 * 
 * This extension function provides a simplified way to schedule repeating tasks without
 * manually creating BukkitRunnable instances. Both the initial delay and repeat period
 * are specified in seconds and automatically converted to Minecraft ticks (20 ticks = 1 second).
 * 
 * The task executes repeatedly at the specified interval until cancelled or the plugin
 * is disabled. All code within the task lambda runs synchronously on the main server thread,
 * making it safe to interact with the Bukkit API and modify game state.
 * 
 * ## Usage Example
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     override fun onEnable() {
 *         // Announce server time every 10 seconds, starting after 5 seconds
 *         runTaskTimer(delay = 5.0, period = 10.0) {
 *             server.broadcastMessage("Current time: ${System.currentTimeMillis()}")
 *         }
 *         
 *         // Heal all players every 30 seconds with cancellation
 *         val healTask = runTaskTimer(delay = 0.0, period = 30.0) {
 *             server.onlinePlayers.forEach { player ->
 *                 player.health = player.maxHealth
 *             }
 *         }
 *         
 *         // Cancel after certain condition
 *         runTaskTimer(delay = 1.0, period = 1.0) {
 *             if (someCondition) {
 *                 cancel() // Stop the repeating task
 *             }
 *         }
 *         
 *         // Countdown timer
 *         var counter = 10
 *         runTaskTimer(delay = 0.0, period = 1.0) {
 *             server.broadcastMessage("Time remaining: $counter")
 *             counter--
 *             if (counter <= 0) {
 *                 server.broadcastMessage("Time's up!")
 *                 cancel()
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * ## Tick Conversion
 * Both delay and period parameters are in seconds and converted to ticks:
 * - Ticks = seconds × 20
 * - Example: delay = 2.0, period = 1.0 → waits 40 ticks, then runs every 20 ticks
 * - Example: delay = 0.0, period = 0.5 → starts immediately, runs every 10 ticks
 * 
 * ## Execution Timeline
 * 1. Initial delay period elapses (delay × 20 ticks)
 * 2. Task executes for the first time
 * 3. Period interval elapses (period × 20 ticks)
 * 4. Task executes again
 * 5. Repeat steps 3-4 until cancelled or plugin disabled
 * 
 * ## Important Notes
 * - The task runs synchronously on the main server thread
 * - First execution occurs after the delay, then repeats every period
 * - Setting delay to 0.0 causes immediate first execution
 * - Minimum practical period is 0.05 seconds (1 tick)
 * - Fractional ticks are truncated (e.g., 1.51 seconds = 30 ticks)
 * - The task has access to BukkitRunnable methods like cancel() and isCancelled()
 * - Tasks are automatically cancelled when the plugin is disabled
 * - For one-time delayed execution, use runTaskLater() instead
 * 
 * ## Thread Safety
 * Since this runs on the main thread, it's safe to:
 * - Modify blocks, entities, and inventory
 * - Send messages to players
 * - Interact with any Bukkit/Paper API
 * - Schedule additional tasks
 * 
 * For asynchronous repeating operations, consider using Bukkit's async timer methods instead.
 * 
 * ## Performance Considerations
 * - Keep task execution time minimal to avoid server lag
 * - Avoid heavy computations in repeating tasks
 * - Use appropriate period intervals (very short periods may impact performance)
 * - Always cancel tasks when no longer needed to free resources
 * 
 * @param delay The initial delay before first execution in seconds. Must be non-negative.
 *              Set to 0.0 for immediate first execution. Converted to ticks by multiplying by 20.
 * @param period The interval between subsequent executions in seconds. Must be positive.
 *               Converted to ticks by multiplying by 20.
 * @param task A lambda function containing the code to execute repeatedly.
 *             Receives BukkitRunnable as receiver, allowing access to scheduling methods
 *             like cancel() to stop the repeating task.
 * @receiver Plugin The plugin that owns this scheduled task. Used for lifecycle management.
 * @return BukkitTask A task object that can be used to cancel the repeating execution
 *                    or check its status.
 * 
 * @see BukkitRunnable
 * @see BukkitTask
 * @see org.bukkit.scheduler.BukkitScheduler.runTaskTimer
 */
inline fun Plugin.runTaskTimer(
  delay: Double,
  period: Double,
  crossinline task: BukkitRunnable.() -> Unit
): BukkitTask {
  val runnable = object : BukkitRunnable() {
    override fun run() = this.task()
  }
  return runnable.runTaskTimer(this, (delay * 20).toLong(), (period * 20).toLong())
}
