package com.peco2282.devcore.config.reflection

import com.peco2282.devcore.config.serializers.Serializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TypeSerializersTest {

  interface Base
  class Implementation : Base

  class BaseSerializer : Serializer<Base> {
    override fun deserialize(value: Any?): Base = Implementation()
    override fun serialize(value: Base): Any = "serialized"
  }

  @Test
  fun testGetInheritedSerializer() {
    TypeSerializers.register(Base::class, BaseSerializer())

    val serializer = TypeSerializers.get(Implementation::class)
    assertNotNull(serializer, "Serializer should be found for implementation class")
    assertEquals("serialized", serializer?.serialize(Implementation()))
  }

  @Test
  fun testSerializeOrRawInherited() {
    TypeSerializers.register(Base::class, BaseSerializer())

    val result = TypeSerializers.serializeOrRaw(Implementation())
    assertEquals("serialized", result, "serializeOrRaw should use inherited serializer")
  }
}
