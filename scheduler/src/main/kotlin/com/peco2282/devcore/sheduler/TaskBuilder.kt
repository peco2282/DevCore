package com.peco2282.devcore.sheduler

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class TaskBuilder(
  plugin: Plugin,
) {
  private val scheduler = plugin.scheduler

  infix fun after(delay: Ticks) = DelayedTask(delay)

  fun now(task: () -> Unit) = scheduler.sync(task)
  fun async(task: () -> Unit) = scheduler.async(task)

  inner class DelayedTask(
    private val delay: Ticks
  ) : Runner {
    infix fun every(period: Ticks) = RepeatingTask(delay, period)

    override infix fun run(task: () -> Unit): TaskHandle =
      scheduler.later(delay, task)
  }

  inner class RepeatingTask(
    private val delay: Ticks,
    private val period: Ticks
  ) : Runner {
    override infix fun run(task: () -> Unit): TaskHandle =
      scheduler.timer(delay, period, task)
  }


}


fun main() {
  val plugin = object : JavaPlugin() {}
  val player = plugin.server.getPlayer("peco2282")!!
  val world = plugin.server.getWorld("world")!!
  // 通常
  plugin.taskCreate after 5.seconds run {
    println("遅延実行")
  }

  player.taskAfter(plugin, 10.seconds) {
    player.sendMessage("まだログインしてたら表示")
  }

// ワールド依存ループ
  world.taskTimer(plugin, 0.ticks, 20.ticks) {
    println("ワールド存続中のみ実行")
  }

}
