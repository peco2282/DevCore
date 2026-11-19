package com.peco2282.devcore.worldpool

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * One loaded disposable world acquired from a [WorldPool].
 *
 * Call [release] exactly once when the match ends. All players still inside are moved to the
 * supplied fallback before the world is unloaded; its copied directory is then deleted off-thread.
 */
class WorldInstance internal constructor(
  val world: World,
  private val pool: WorldPool,
  internal val directory: File
) {
  fun release(fallback: Location): CompletableFuture<Unit> = pool.release(this, fallback)
}

/**
 * Creates isolated disposable worlds by copying [template].
 *
 * ```kotlin
 * val pool = plugin.worldPool(File("templates/arena"))
 * pool.acquire().thenAccept { instance ->
 *   startMatch(instance.world)
 *   // later: instance.release(lobbySpawn)
 * }
 * ```
 *
 * File copying/deletion uses `CompletableFuture` worker threads. Bukkit world creation, unloading,
 * and player teleports are marshalled onto the main thread. The template remains untouched;
 * volatile identity/lock/player files are excluded by the copy helper.
 *
 * Use [close] with a fallback while instances remain. Parameterless [close] is only valid after all
 * instances were released and exists for `AutoCloseable` integration. The pool cannot be reopened.
 */
class WorldPool internal constructor(
  private val plugin: Plugin,
  private val template: File,
  private val instancesRoot: File
) : AutoCloseable {
  private val instances = linkedSetOf<WorldInstance>()
  @Volatile private var closed = false

  init {
    require(template.isDirectory) { "Template must be a world directory" }
    require(instancesRoot.canonicalFile == plugin.server.worldContainer.canonicalFile) {
      "instancesRoot must be the server world container so Bukkit can load copied worlds"
    }
    instancesRoot.mkdirs()
  }

  /** Copies the template, loads the copy, tracks it, and completes with its handle. */
  fun acquire(): CompletableFuture<WorldInstance> {
    check(!closed) { "WorldPool is closed" }
    val name = "devcore-instance-${UUID.randomUUID()}"
    val target = File(instancesRoot, name)
    return CompletableFuture.runAsync { copyWorldTemplate(template.toPath(), target.toPath()) }
      .thenCompose { onMainThread { requireNotNull(Bukkit.createWorld(WorldCreator(target.name))) { "Could not load $name" } } }
      .thenApply { world -> WorldInstance(world, this, target).also { synchronized(instances) { instances += it } } }
  }

  internal fun release(instance: WorldInstance, fallback: Location): CompletableFuture<Unit> =
    onMainThread {
      require(fallback.world != instance.world) { "Fallback must be in another world" }
      instance.world.players.forEach { it.teleport(fallback) }
      check(Bukkit.unloadWorld(instance.world, false)) { "Could not unload ${instance.world.name}" }
      synchronized(instances) { instances -= instance }
    }.thenApplyAsync {
      deleteWorldDirectory(instance.directory.toPath(), instancesRoot.toPath())
      Unit
    }

  /** Releases every active instance using [fallback], then completes when deletion finishes. */
  fun close(fallback: Location): CompletableFuture<Unit> {
    closed = true
    val pending = synchronized(instances) { instances.toList() }.map { release(it, fallback) }
    return CompletableFuture.allOf(*pending.toTypedArray()).thenApply { Unit }
  }

  override fun close() { check(instances.isEmpty()) { "Use close(fallback) while instances are active" }; closed = true }

  private fun <T> onMainThread(action: () -> T): CompletableFuture<T> {
    if (Bukkit.isPrimaryThread()) return CompletableFuture.completedFuture(action())
    val result = CompletableFuture<T>()
    plugin.server.scheduler.runTask(plugin, Runnable {
      try { result.complete(action()) } catch (failure: Throwable) { result.completeExceptionally(failure) }
    })
    return result
  }
}

/**
 * Creates a pool rooted in the server world container.
 *
 * Bukkit can only load copied worlds from that container, so custom [instancesRoot] values are
 * accepted only when they resolve to the same canonical directory.
 */
fun Plugin.worldPool(template: File, instancesRoot: File = server.worldContainer): WorldPool =
  WorldPool(this, template, instancesRoot)
