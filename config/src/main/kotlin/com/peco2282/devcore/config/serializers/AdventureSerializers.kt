package com.peco2282.devcore.config.serializers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

object AdventureSerializers {
  val COMPONENT: Serializer<Component> = ComponentSerializer()

  val TEXT_COLOR: Serializer<TextColor> = object : Serializer<TextColor> {
    override fun deserialize(value: Any?): TextColor {
      return TextColor.fromHexString(value.toString())
        ?: throw IllegalArgumentException("Invalid hex color: $value")
    }

    override fun serialize(value: TextColor): Any {
      return value.asHexString()
    }
  }

  fun registerAll() {
    Serializer.registerer(Component::class, COMPONENT)
    Serializer.registerer(TextColor::class, TEXT_COLOR)
  }
}
