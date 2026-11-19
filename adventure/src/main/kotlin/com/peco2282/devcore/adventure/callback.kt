package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.Styler
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

fun Styler.onClick(uses: Int = 1, lifetime: Duration, callback: (Audience) -> Unit): Styler {
  require(uses > 0) { "Uses must be positive" }
  require(lifetime.isPositive()) { "Lifetime must be positive" }
  val options = ClickCallback.Options.builder().uses(uses).lifetime(lifetime.toJavaDuration()).build()
  return clickEvent(ClickEvent.callback({ callback(it) }, options))
}

class ConfirmBuilder internal constructor() {
  var timeout: Duration = 30.seconds
  private var yesLabel: Component = Component.text("[Yes]")
  private var noLabel: Component = Component.text("[No]")
  private var yesAction: (Audience) -> Unit = {}
  private var noAction: (Audience) -> Unit = {}

  fun yes(label: Component, action: (Audience) -> Unit) { yesLabel = label; yesAction = action }
  fun no(label: Component, action: (Audience) -> Unit) { noLabel = label; noAction = action }

  internal fun build(question: Component): Component {
    val completed = AtomicBoolean(false)
    fun button(label: Component, action: (Audience) -> Unit): Component = label.clickEvent(
      ClickEvent.callback(
        { clicked -> if (completed.compareAndSet(false, true)) action(clicked) },
        ClickCallback.Options.builder().uses(1).lifetime(timeout.toJavaDuration()).build())
    )
    return Component.text().append(question).append(Component.space()).append(button(yesLabel, yesAction))
      .append(Component.space()).append(button(noLabel, noAction)).build()
  }
}

fun Audience.confirm(question: Component, block: ConfirmBuilder.() -> Unit) {
  sendMessage(ConfirmBuilder().apply(block).build(question))
}
