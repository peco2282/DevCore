package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

fun Audience.send(component: Component) = sendMessage(component)
fun Collection<Audience>.send(component: Component) = forEach { it.sendMessage(component) }

fun Audience.send(consumer: ComponentBuilder.() -> Unit) = sendMessage(component(consumer))
fun Collection<Audience>.send(consumer: ComponentBuilder.() -> Unit) = forEach { it.sendMessage(component(consumer)) }

fun Audience.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) =
  sendMessage(component(joiner, consumer))

fun Collection<Audience>.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) =
  forEach { it.sendMessage(component(joiner, consumer)) }

