package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.Componenter
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

fun Audience.send(component: Component) = sendMessage(component)
fun Collection<Audience>.send(component: Component) = forEach { it.sendMessage(component) }

fun Audience.send(consumer: Componenter.() -> Unit) = sendMessage(component(consumer))
fun Collection<Audience>.send(consumer: Componenter.() -> Unit) = forEach { it.sendMessage(component(consumer)) }

inline fun Audience.send(noinline joiner: (Componenter) -> Component = Componenter::join, noinline consumer: Componenter.() -> Unit) =
  sendMessage(component(joiner, consumer))

inline fun Collection<Audience>.send(noinline joiner: (Componenter) -> Component = Componenter::join, noinline consumer: Componenter.() -> Unit) =
  forEach { it.sendMessage(component(joiner, consumer)) }

