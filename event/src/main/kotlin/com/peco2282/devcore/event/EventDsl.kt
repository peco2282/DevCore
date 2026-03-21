package com.peco2282.devcore.event

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.player.PlayerEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import java.util.logging.Level

/**
 * A DSL builder for handling Bukkit events.
 *
 * @param T The type of the event.
 * @param plugin The plugin instance.
 * @param type The class of the event.
 * @param priority The priority of the event listener (default: NORMAL).
 * @param ignoreCancelled Whether to ignore cancelled events (default: false).
 */
class EventBuilder<T : Event>(
  val plugin: Plugin,
  val type: Class<T>,
  var priority: EventPriority = EventPriority.NORMAL,
  var ignoreCancelled: Boolean = false
) {
  private val filters = mutableListOf<T.() -> Boolean>()
  private var handler: (T.() -> Unit)? = null
  private var once = false
  private var debug = false
  private var registeredListener: RegisteredListener? = null
  private var isRegistered = false

  /**
   * Enables debug mode for this listener.
   */
  fun debug(): EventBuilder<T> = apply {
    debug = true
  }

  /**
   * Adds a filter condition to the event handler.
   */
  fun filter(predicate: T.() -> Boolean): EventBuilder<T> {
    filters.add(predicate)
    return this
  }

  /**
   * Configures the listener to unregister itself after the first successful execution.
   */
  fun once(): EventBuilder<T> = apply {
    once = true
  }

  /**
   * Configures the listener to unregister itself after a certain number of executions.
   *
   * @param times The number of times to handle the event.
   */
  fun take(times: Int): EventBuilder<T> = apply {
    var count = 0
    filter {
      count++
      if (count >= times) unregister()
      count <= times
    }
  }

  /**
   * Keeps the listener registered as long as the predicate returns true.
   *
   * @param predicate A predicate that determines whether to keep the listener.
   */
  fun takeWhile(predicate: T.() -> Boolean): EventBuilder<T> = filter {
    val result = predicate()
    if (!result) unregister()
    result
  }

  /**
   * Filters the event based on the player associated with it (only for [PlayerEvent]).
   *
   * @param predicate A predicate applied to the player.
   */
  fun filterPlayer(predicate: Player.() -> Boolean): EventBuilder<T> = filter {
    if (this is PlayerEvent) {
      predicate(player)
    } else {
      true
    }
  }

  /**
   * Cancels the event if it is [Cancellable].
   */
  fun T.cancel() {
    if (this is Cancellable) {
      isCancelled = true
    }
  }

  /**
   * Automatically unregisters the listener after a specified number of seconds.
   *
   * @param seconds The duration in seconds.
   */
  fun expireAfter(seconds: Long): EventBuilder<T> = apply {
    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
      unregister()
    }, seconds * 20)
  }

  /**
   * Defines the action to be performed when the event is handled.
   * Calling this method will register the listener.
   *
   * @param action The action block.
   */
  fun handle(action: T.() -> Unit): EventBuilder<T> = apply {
    this.handler = action
    register()
  }

  /**
   * Manually unregisters the listener.
   */
  fun unregister() {
    if (!isRegistered) return
    val handlers = getHandlerList(type)
    registeredListener?.let { handlers.unregister(it) }
    isRegistered = false
  }

  private fun register() {
    if (isRegistered) return
    val listener = object : Listener {}
    val executor = { _: Listener, event: Event ->
      if (type.isInstance(event)) {
        @Suppress("UNCHECKED_CAST")
        val e = event as T
        if (debug) {
          plugin.logger.log(Level.INFO, "[EventDSL Debug] Handling event: ${type.simpleName}")
        }
        if (filters.all { it(e) }) {
          handler?.invoke(e)
          if (once) {
            unregister()
          }
        } else if (debug) {
          plugin.logger.log(Level.INFO, "[EventDSL Debug] Event filtered: ${type.simpleName}")
        }
      }
    }

    Bukkit.getPluginManager().registerEvent(
      type,
      listener,
      priority,
      executor,
      plugin,
      ignoreCancelled
    )

    // Identify and hold the RegisteredListener for unregistering.
    val handlers = getHandlerList(type)
    registeredListener = handlers.registeredListeners.find { it.listener === listener }
    isRegistered = true
  }

  private fun getHandlerList(clazz: Class<out Event>): HandlerList {
    return try {
      val method = clazz.getMethod("getHandlerList")
      method.invoke(null) as HandlerList
    } catch (e: Exception) {
      // Find getHandlerList in parent classes if not present in the class itself.
      clazz.methods.find { it.name == "getHandlerList" }?.invoke(null) as? HandlerList
        ?: throw IllegalStateException("Event ${clazz.name} does not have getHandlerList() method.")
    }
  }
}

/**
 * Defines an event listener using a DSL.
 *
 * Example usage:
 * ```kotlin
 * on<PlayerJoinEvent> {
 *     filter { player.isOp }
 *     handle {
 *         player.sendMessage("Welcome, OP!")
 *     }
 * }
 * ```
 *
 * @param T The type of the event.
 * @param priority The priority of the event listener.
 * @param ignoreCancelled Whether to ignore cancelled events.
 * @param block The builder block.
 * @return The [EventBuilder] instance.
 */
inline fun <reified T : Event> Plugin.on(
  priority: EventPriority = EventPriority.NORMAL,
  ignoreCancelled: Boolean = false,
  noinline block: EventBuilder<T>.() -> Unit
): EventBuilder<T> {
  return EventBuilder(this, T::class.java, priority, ignoreCancelled).apply(block)
}

/**
 * Defines an event listener that executes only once.
 *
 * @param T The type of the event.
 * @param priority The priority of the event listener.
 * @param ignoreCancelled Whether to ignore cancelled events.
 * @param action The action block.
 * @return The [EventBuilder] instance.
 */
inline fun <reified T : Event> Plugin.listenOnce(
  priority: EventPriority = EventPriority.NORMAL,
  ignoreCancelled: Boolean = false,
  noinline action: T.() -> Unit
): EventBuilder<T> {
  return on(priority, ignoreCancelled) {
    once()
    handle(action)
  }
}

/**
 * A group of event listeners for easier management (e.g., bulk unregistering).
 */
class EventGroup(val plugin: Plugin) {
  val builders = mutableListOf<EventBuilder<*>>()

  /**
   * Registers an event listener within this group.
   */
  inline fun <reified T : Event> on(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline block: EventBuilder<T>.() -> Unit
  ): EventBuilder<T> {
    return EventBuilder(plugin, T::class.java, priority, ignoreCancelled).apply(block).also {
      builders.add(it)
    }
  }

  /**
   * Registers a one-time event listener within this group.
   */
  inline fun <reified T : Event> listenOnce(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline action: T.() -> Unit
  ): EventBuilder<T> {
    return on(priority, ignoreCancelled) {
      once()
      handle(action)
    }
  }

  /**
   * Unregisters all event listeners in this group.
   */
  fun unregisterAll() {
    builders.forEach { it.unregister() }
    builders.clear()
  }
}

/**
 * Defines multiple event listeners as a group.
 *
 * Example usage:
 * ```kotlin
 * val group = events {
 *     on<PlayerQuitEvent> {
 *         handle { println("${player.name} left.") }
 *     }
 *     listenOnce<PlayerJoinEvent> {
 *         println("First player joined!")
 *     }
 * }
 * // Later...
 * group.unregisterAll()
 * ```
 */
fun Plugin.events(block: EventGroup.() -> Unit): EventGroup {
  return EventGroup(this).apply(block)
}
