package com.peco2282.devcore.sheduler

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

class BukkitDispatcher(val plugin: Plugin) : CoroutineDispatcher() {
  override fun dispatch(context: CoroutineContext, block: Runnable) {
    plugin.scheduler.sync { block.run() }
  }
}
