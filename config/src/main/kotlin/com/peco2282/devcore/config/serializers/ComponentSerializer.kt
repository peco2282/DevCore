package com.peco2282.devcore.config.serializers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class ComponentSerializer : Serializer<Component> {

  override fun deserialize(value: Any?): Component {
    return MiniMessage.miniMessage().deserialize(value.toString())
  }

  override fun serialize(value: Component): Any {
    return MiniMessage.miniMessage().serialize(value)
  }
}
