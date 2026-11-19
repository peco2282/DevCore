package com.peco2282.devcore.storage

import com.peco2282.devcore.config.reflection.ClassMapper
import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** What to do when a player's stored data exists but cannot be read. */
enum class LoadFailure {
  /**
   * Refuse the login (default). Playing on default data would overwrite the real data on the next
   * save, so the safe choice is to keep the player out until an admin has looked at it.
   */
  KICK,

  /** Set the broken data aside (see [DataBackend.quarantine]) and continue with defaults. */
  USE_DEFAULT
}

/**
 * Per-player data that is loaded before the player joins and saved when they leave.
 *
 * ```kotlin
 * data class Stats(var kills: Int = 0, var coins: Long = 0)
 *
 * val stats = plugin.playerData<Stats>()
 * stats[player].kills++          // plain object access, no I/O on the main thread
 * ```
 *
 * - Loaded in `AsyncPlayerPreLoginEvent`, so [get] works from `PlayerJoinEvent` on.
 * - Saved asynchronously on quit, periodically (autosave) and, synchronously, when the plugin disables.
 * - Writes for one player are applied in order, and a login waits for that player's pending save,
 *   so quickly re-joining cannot read stale data.
 *
 * Mutable state in [V] must only be changed from the main thread.
 */
class PlayerData<V : Any> @PublishedApi internal constructor(
  val plugin: Plugin,
  private val backend: DataBackend<V>,
  private val default: (UUID) -> V,
  private val onLoadFailure: LoadFailure,
  private val loadFailureMessage: Component,
  autosave: Duration
) {
  private val cache = ConcurrentHashMap<UUID, V>()
  private val pending = ConcurrentHashMap<UUID, CompletableFuture<Void>>()
  private val io: ExecutorService = Executors.newSingleThreadExecutor { r ->
    Thread(r, "devcore-storage-${plugin.name}").apply { isDaemon = true }
  }
  private val listener = DataListener()

  @Volatile
  private var closed = false

  init {
    Bukkit.getPluginManager().registerEvents(listener, plugin)
    if (autosave.isPositive()) {
      val period = Ticks((autosave.inWholeMilliseconds / 50).coerceAtLeast(20))
      plugin.scheduler.timer(period, period) { saveAll() }
    }
  }

  /** The loaded data of [player]. @throws IllegalStateException if it is not loaded (e.g. before login completed). */
  operator fun get(player: Player): V = get(player.uniqueId)

  /** The loaded data of an online player by id. @throws IllegalStateException if not loaded. */
  operator fun get(id: UUID): V =
    cache[id] ?: throw IllegalStateException("Data for $id is not loaded. It is available from PlayerJoinEvent until the player quits.")

  /** The loaded data, or `null` if the player is not online / loaded. */
  fun getOrNull(id: UUID): V? = cache[id]

  /** Whether [id]'s data is loaded. */
  fun isLoaded(id: UUID): Boolean = cache.containsKey(id)

  /**
   * Saves [id]'s data. The snapshot is taken now, on the calling thread; only the write is asynchronous.
   * Completes immediately if nothing is loaded for [id].
   */
  fun save(id: UUID): CompletableFuture<Void> {
    val value = cache[id] ?: return CompletableFuture.completedFuture(null)
    return enqueueWrite(id, backend.encode(value))
  }

  /** Saves everyone currently loaded. */
  fun saveAll(): CompletableFuture<Void> =
    CompletableFuture.allOf(*cache.keys.map { save(it) }.toTypedArray())

  /**
   * Reads data for a player who may be offline, without caching it. For an online player this is the live object.
   * Completes with `null` if the player has no stored data.
   */
  fun peek(id: UUID): CompletableFuture<V?> {
    cache[id]?.let { return CompletableFuture.completedFuture(it) }
    return CompletableFuture.supplyAsync({ readStored(id) }, io)
  }

  /**
   * Changes a player's data whether they are online or not, then saves it.
   *
   * For an online player, [change] runs immediately on the calling thread. For an offline one it runs on
   * the storage thread (don't touch the Bukkit API in it), with defaults if there is no stored data.
   */
  fun edit(id: UUID, change: (V) -> Unit): CompletableFuture<Void> {
    cache[id]?.let {
      change(it)
      return save(id)
    }
    return CompletableFuture.runAsync({
      val value = readStored(id) ?: default(id)
      change(value)
      backend.write(id, backend.encode(value))
    }, io)
  }

  /** Deletes stored data for an offline player. Online players' data is left alone. */
  fun delete(id: UUID): CompletableFuture<Void> {
    if (cache.containsKey(id)) return CompletableFuture.failedFuture(IllegalStateException("$id is online"))
    return CompletableFuture.runAsync({ backend.delete(id) }, io)
  }

  /**
   * Saves everyone, waits for the writes (up to 10 seconds), stops handling events and shuts down the storage thread.
   * Called automatically when the plugin is disabled.
   */
  fun close() {
    if (closed) return
    closed = true
    HandlerList.unregisterAll(listener)
    try {
      saveAll().get(10, TimeUnit.SECONDS)
    } catch (e: Exception) {
      plugin.logger.severe("Could not finish saving player data: ${e.message}")
    }
    io.shutdown()
    io.awaitTermination(5, TimeUnit.SECONDS)
    cache.clear()
  }

  private fun enqueueWrite(id: UUID, text: String): CompletableFuture<Void> {
    if (io.isShutdown) return CompletableFuture.failedFuture(IllegalStateException("storage is closed"))
    val future = CompletableFuture.runAsync({ backend.write(id, text) }, io)
    pending[id] = future
    future.whenComplete { _, error ->
      pending.remove(id, future)
      if (error != null) plugin.logger.severe("Failed to save data for $id: ${error.cause?.message ?: error.message}")
    }
    return future
  }

  /** Reads and decodes; a corrupt entry is quarantined and reported as an exception. */
  private fun readStored(id: UUID): V? {
    val text = backend.read(id) ?: return null
    return try {
      backend.decode(text)
    } catch (e: Exception) {
      backend.quarantine(id)
      throw StorageException("Stored data for $id could not be read: ${e.message}", e)
    }
  }

  private inner class DataListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
      if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return
      val id = event.uniqueId
      if (cache.containsKey(id)) return // a second session of an online player: keep the live data
      try {
        pending[id]?.join() // a quick re-join must see the previous session's save
        val value = try {
          readStored(id) ?: default(id)
        } catch (e: StorageException) {
          if (onLoadFailure == LoadFailure.KICK) throw e
          plugin.logger.warning("${e.message} Using defaults; the old data was set aside.")
          default(id)
        }
        cache[id] = value
      } catch (e: Exception) {
        plugin.logger.severe("Could not load data for $id: ${e.message}")
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, loadFailureMessage)
      }
    }

    /** Another plugin or the server may still refuse the login after pre-login; don't keep orphaned data. */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: PlayerLoginEvent) {
      if (event.result != PlayerLoginEvent.Result.ALLOWED && Bukkit.getPlayer(event.player.uniqueId) == null) {
        cache.remove(event.player.uniqueId)
      }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
      val id = event.player.uniqueId
      val current = Bukkit.getPlayer(id)
      if (current == null || current === event.player) {
        val value = cache.remove(id) ?: return
        enqueueWrite(id, backend.encode(value))
      } else {
        save(id) // an older session of the same player ended; the new session keeps the cached data
      }
    }

    @EventHandler
    fun onDisable(event: PluginDisableEvent) {
      if (event.plugin === plugin) close()
    }
  }
}

/** Stored data could not be read. */
class StorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Creates [PlayerData] stored as YAML files in [dir].
 *
 * @param default Data for a player with nothing stored. By default the data class is built with its default values.
 * @param autosave Interval of the periodic save; zero disables it.
 */
inline fun <reified V : Any> Plugin.playerData(
  dir: File = File(dataFolder, "playerdata"),
  autosave: Duration = 5.minutes,
  onLoadFailure: LoadFailure = LoadFailure.KICK,
  loadFailureMessage: Component = Component.text("Your data could not be loaded. Please contact an administrator."),
  noinline default: (UUID) -> V = { ClassMapper.create(V::class, YamlConfiguration()) }
): PlayerData<V> = PlayerData(this, yamlBackend<V>(dir), default, onLoadFailure, loadFailureMessage, autosave)

/** Creates [PlayerData] on a custom [backend]. */
fun <V : Any> Plugin.playerData(
  backend: DataBackend<V>,
  default: (UUID) -> V,
  autosave: Duration = 5.minutes,
  onLoadFailure: LoadFailure = LoadFailure.KICK,
  loadFailureMessage: Component = Component.text("Your data could not be loaded. Please contact an administrator.")
): PlayerData<V> = PlayerData(this, backend, default, onLoadFailure, loadFailureMessage, autosave)
