package com.peco2282.devcore.config.serializers

import com.peco2282.devcore.config.reflection.TypeSerializers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializersIntegrationTest {

    @Test
    fun testAdventureSerializersRegistration() {
        AdventureSerializers.registerAll()

        assertTrue(TypeSerializers.has(Component::class))
        assertTrue(TypeSerializers.has(TextColor::class))
        
        val component = MiniMessage.miniMessage().deserialize("<red>Hello")
        val serializedComponent = TypeSerializers.serializeOrRaw(component)
        assertEquals("<red>Hello", serializedComponent)
        
        val textColor = TextColor.color(0xFF0000)
        val serializedColor = TypeSerializers.serializeOrRaw(textColor)
        assertEquals("#ff0000", serializedColor)
    }

    @Test
    fun testBukkitSerializersRegistration() {
        BukkitSerializers.registerAll()

        assertTrue(TypeSerializers.has(ItemStack::class))
        assertTrue(TypeSerializers.has(Location::class))
        assertTrue(TypeSerializers.has(Vector::class))
    }
}
