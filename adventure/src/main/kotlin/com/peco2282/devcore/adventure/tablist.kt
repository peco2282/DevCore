package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

class TabListBuilder {
  internal var header: Component = Component.empty()
  internal var footer: Component = Component.empty()
  fun header(block: ComponentBuilder.() -> Unit) { header = component(block) }
  fun footer(block: ComponentBuilder.() -> Unit) { footer = component(block) }
}

fun Audience.sendTabList(block: TabListBuilder.() -> Unit) {
  val value = TabListBuilder().apply(block)
  sendPlayerListHeaderAndFooter(value.header, value.footer)
}

fun Collection<Audience>.sendTabList(block: TabListBuilder.() -> Unit) {
  val value = TabListBuilder().apply(block)
  forEach { it.sendPlayerListHeaderAndFooter(value.header, value.footer) }
}
