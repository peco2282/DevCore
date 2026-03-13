package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scoreboard.api.factory.ScoreboardFactory
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.CopyOnWriteArrayList

object ScoreboardApi : Listener {
  private val handles = CopyOnWriteArrayList<Handle>()
  private var isInitialized = false
  @PublishedApi
  internal var factoryInstance: ScoreboardFactory? = null

  /**
   * ScoreboardApiを初期化する。
   * 自動クリーンアップを有効にするために必要。
   */
  fun init(plugin: Plugin, factory: ScoreboardFactory) {
    this.factoryInstance = factory
    if (isInitialized) return
    Bukkit.getPluginManager().registerEvents(this, plugin)
    isInitialized = true
  }

  fun <F : ScoreboardFactory> getFactory(): F {
    val f = factoryInstance ?: throw IllegalStateException("ScoreboardApi is not initialized. Call init() first.")
    @Suppress("UNCHECKED_CAST")
    return (f as? F) ?: throw IllegalStateException("Factory is not of requested type: ${f::class.simpleName}")
  }

  inline fun <reified F : ScoreboardFactory> findFactory(): F? {
    return factoryInstance as? F
  }

  fun factory(): ScoreboardFactory {
    return factoryInstance ?: throw IllegalStateException("ScoreboardApi is not initialized. Call init() first.")
  }

  fun register(handle: Handle) {
    handles.add(handle)
  }

  fun unregister(handle: Handle) {
    handles.remove(handle)
  }

  /**
   * 全ての登録されたHandleを破棄する。
   * プラグインのonDisableなどで呼び出すことを推奨。
   */
  fun destroyAll() {
    handles.forEach { it.destroy() }
    handles.clear()
  }

  @EventHandler
  fun onQuit(event: PlayerQuitEvent) {
    handles.forEach { it.hide(event.player) }
  }
}
