package com.peco2282.devcore.worldpool

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture

class WorldInstance internal constructor(
  val world: World,
  private val pool: WorldPool,
  internal val directory: File
) {
  fun release(fallback: Location): CompletableFuture<Unit> = pool.release(this, fallback)
}

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

fun Plugin.worldPool(template: File, instancesRoot: File = server.worldContainer): WorldPool =
  WorldPool(this, template, instancesRoot)
