package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scoreboard.api.factory.ScoreboardFactory
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ScoreboardApi is the entry point for the scoreboard and boss bar system.
 * It manages [Handle] instances and provides access to the [ScoreboardFactory].
 */
object ScoreboardApi : Listener {
  private val handles = CopyOnWriteArrayList<Handle>()
  private var isInitialized = false

  @PublishedApi
  internal var factoryInstance: ScoreboardFactory? = null

  /**
   * Initializes the ScoreboardApi.
   * This must be called before using other methods to enable automatic cleanup.
   *
   * @param plugin The plugin instance for event registration.
   * @param factory The scoreboard factory implementation (e.g., NMS based). If null, a default Paper API based factory will be used.
   */
  fun init(plugin: Plugin, factory: ScoreboardFactory? = null) {
    this.factoryInstance = factory ?: PaperScoreboardFactory()
    if (isInitialized) return
    Bukkit.getPluginManager().registerEvents(this, plugin)
    isInitialized = true
  }

  /**
   * Retrieves the current factory instance, cast to the specified type.
   *
   * @return The current [ScoreboardFactory] instance.
   * @throws IllegalStateException If ScoreboardApi is not initialized.
   */
  fun <F : ScoreboardFactory> getFactory(): F {
    val f = factoryInstance ?: throw IllegalStateException("ScoreboardApi is not initialized. Call init() first.")
    @Suppress("UNCHECKED_CAST")
    return (f as? F) ?: throw IllegalStateException("Factory is not of requested type: ${f::class.simpleName}")
  }

  /**
   * Finds the current factory instance if it matches the specified type.
   *
   * @return The current [ScoreboardFactory] instance, or null if it's not the requested type.
   */
  inline fun <reified F : ScoreboardFactory> findFactory(): F? {
    return factoryInstance as? F
  }

  /**
   * Retrieves the current factory instance.
   *
   * @return The current [ScoreboardFactory] instance.
   * @throws IllegalStateException If ScoreboardApi is not initialized.
   */
  fun factory(): ScoreboardFactory {
    return factoryInstance ?: throw IllegalStateException("ScoreboardApi is not initialized. Call init() first.")
  }

  /**
   * Registers a [Handle] to be managed by the API.
   *
   * @param handle The handle to register.
   */
  fun register(handle: Handle) {
    handles.add(handle)
  }

  /**
   * Unregisters a [Handle] from the API.
   *
   * @param handle The handle to unregister.
   */
  fun unregister(handle: Handle) {
    handles.remove(handle)
  }

  /**
   * Destroys all registered handles.
   * Recommended to be called in the plugin's `onDisable` method.
   */
  fun destroyAll() {
    handles.forEach { it.destroy() }
    handles.clear()
  }

  /**
   * Internal event handler to hide boards when a player quits.
   */
  @EventHandler
  fun onQuit(event: PlayerQuitEvent) {
    handles.forEach { it.hide(event.player) }
  }
}
