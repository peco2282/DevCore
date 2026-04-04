package com.peco2282.devcore.packet

import io.netty.channel.ChannelHandlerContext

/**
 * Provides hooks for intercepting and transforming packets at the Netty pipeline level.
 *
 * Use [on], [onSend], or [onReceive] for type-safe packet event handling,
 * and [transformSend] / [transformReceive] for low-level packet mutation.
 */
interface PacketListener {
  /**
   * Registers a handler invoked for every packet read (received) from the client.
   *
   * @param handler Called with the [ChannelHandlerContext] and the raw packet object.
   */
  fun onRead(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit)

  /**
   * Registers a handler invoked for every packet written (sent) to the client.
   *
   * @param handler Called with the [ChannelHandlerContext] and the raw packet object.
   */
  fun onWrite(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit)

  /**
   * Registers a type-safe handler for both sent and received packets of type [T].
   *
   * @param T The packet type to listen for.
   * @param type The [Class] of the packet.
   * @param action Called on the packet instance with the associated [PacketEvent].
   */
  fun <T : Any> on(type: Class<T>, action: T.(PacketEvent) -> Unit)

  /**
   * Registers a type-safe handler for packets of type [T] being sent to the client.
   *
   * @param T The packet type to listen for.
   * @param type The [Class] of the packet.
   * @param action Called on the packet instance with the associated [PacketEvent].
   */
  fun <T : Any> onSend(type: Class<T>, action: T.(PacketEvent) -> Unit)

  /**
   * Registers a type-safe handler for packets of type [T] received from the client.
   *
   * @param T The packet type to listen for.
   * @param type The [Class] of the packet.
   * @param action Called on the packet instance with the associated [PacketEvent].
   */
  fun <T : Any> onReceive(type: Class<T>, action: T.(PacketEvent) -> Unit)

  /**
   * Intercepts and transforms packets being sent at the Netty level.
   *
   * @param T The packet type to transform.
   * @param transformer A function that receives the original packet and returns the replacement.
   */
  fun <T : Any> transformSend(transformer: (T) -> T)

  /**
   * Intercepts and transforms received packets at the Netty level.
   *
   * @param T The packet type to transform.
   * @param transformer A function that receives the original packet and returns the replacement.
   */
  fun <T : Any> transformReceive(transformer: (T) -> T)

  /**
   * Enables or disables console logging of all packets passing through the pipeline.
   *
   * @param enabled `true` to enable logging, `false` to disable.
   */
  fun logPackets(enabled: Boolean)

  /**
   * Enables or disables latency measurement for packets.
   *
   * @param enabled `true` to enable measurement, `false` to disable.
   */
  fun measureLatency(enabled: Boolean)
}

/**
 * Registers a type-safe handler for both sent and received packets of type [T].
 *
 * Reified inline variant of [PacketListener.on].
 */
inline fun <reified T : Any> PacketListener.on(noinline action: T.(PacketEvent) -> Unit) {
  on(T::class.java, action)
}

/**
 * Registers a type-safe handler for packets of type [T] being sent to the client.
 *
 * Reified inline variant of [PacketListener.onSend].
 */
inline fun <reified T : Any> PacketListener.onSend(noinline action: T.(PacketEvent) -> Unit) {
  onSend(T::class.java, action)
}

/**
 * Registers a type-safe handler for packets of type [T] received from the client.
 *
 * Reified inline variant of [PacketListener.onReceive].
 */
inline fun <reified T : Any> PacketListener.onReceive(noinline action: T.(PacketEvent) -> Unit) {
  onReceive(T::class.java, action)
}
