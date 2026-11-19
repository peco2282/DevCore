package com.peco2282.devcore.debug

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.WeakHashMap

class DebugController internal constructor(private val sink: (String) -> Unit) {
  private val enabled = mutableSetOf<String>()
  @Synchronized fun enable(category: String) { enabled += normalized(category) }
  @Synchronized fun disable(category: String) { enabled -= normalized(category) }
  @Synchronized fun isEnabled(category: String) = normalized(category) in enabled
  fun logger(category: String) = DebugLogger(normalized(category), this, sink)
  fun categories(): Set<String> = synchronized(this) { enabled.toSet() }
  private fun normalized(category: String): String {
    require(category.matches(Regex("[a-zA-Z0-9._-]+"))) { "Invalid debug category" }
    return category.lowercase()
  }
}

class DebugLogger internal constructor(
  private val category: String,
  private val controller: DebugController,
  private val sink: (String) -> Unit,
  private val nanoTime: () -> Long = System::nanoTime
) {
  fun debug(message: () -> String) { if (controller.isEnabled(category)) sink("[$category] ${message()}") }
  fun <T> timed(label: String, block: () -> T): T {
    val started = nanoTime()
    try { return block() } finally { debug { "$label took ${"%.3f".format((nanoTime() - started) / 1_000_000.0)}ms" } }
  }
  fun tps(): DoubleArray = Bukkit.getTPS()
}

private val controllers = WeakHashMap<Plugin, DebugController>()
fun Plugin.debugController(): DebugController = synchronized(controllers) {
  controllers.getOrPut(this) { DebugController { logger.info(it) } }
}
fun Plugin.debugLogger(category: String): DebugLogger = debugController().logger(category)

/** Creates a controller with a custom sink, useful for tests and embedded tooling. */
fun debugController(sink: (String) -> Unit): DebugController = DebugController(sink)
