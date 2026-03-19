package com.peco2282.devcore.packet

import io.netty.channel.ChannelHandlerContext

interface PacketListener {
  fun onRead(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit)
  fun onWrite(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit)

  fun <T : Any> on(type: Class<T>, action: T.(PacketEvent) -> Unit)
  fun <T : Any> onSend(type: Class<T>, action: T.(PacketEvent) -> Unit)
  fun <T : Any> onReceive(type: Class<T>, action: T.(PacketEvent) -> Unit)

  /**
   * 送信されるパケットをNettyレベルで横取りして書き換えます。
   */
  fun <T : Any> transformSend(transformer: (T) -> T)

  /**
   * 受信したパケットをNettyレベルで横取りして書き換えます。
   */
  fun <T : Any> transformReceive(transformer: (T) -> T)

    fun logPackets(enabled: Boolean)

    fun measureLatency(enabled: Boolean)
}

inline fun <reified T : Any> PacketListener.on(noinline action: T.(PacketEvent) -> Unit) {
  on(T::class.java, action)
}

inline fun <reified T : Any> PacketListener.onSend(noinline action: T.(PacketEvent) -> Unit) {
  onSend(T::class.java, action)
}

inline fun <reified T : Any> PacketListener.onReceive(noinline action: T.(PacketEvent) -> Unit) {
  onReceive(T::class.java, action)
}