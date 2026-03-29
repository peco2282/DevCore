package com.peco2282.devcore.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

internal fun <T> MutableList<T>.updateLast(transformer: (T) -> T) {
  if (isEmpty()) return
  this[lastIndex] = transformer(this[lastIndex])
}

/**
 * Represents different component serialization formats supported by Adventure API.
 *
 * @property serializer The component serializer instance for this type.
 */

enum class ComponentType(private val serializer: ComponentSerializer<Component, out Component, String>) {

  /**
   * Legacy formatting using section sign (§) as the formatting character.
   * Commonly used in older Minecraft servers and plugins.
   * Example: "§aGreen text §lBold"
   */
  LEGACY_SECTION(LegacyComponentSerializer.legacySection()),

  /**
   * Legacy formatting using ampersand (&) as the formatting character.
   * Often used as a more convenient alternative to section signs.
   * Example: "&aGreen text &lBold"
   */
  LEGACY_AMPERSAND(LegacyComponentSerializer.legacyAmpersand()),

  /**
   * Modern MiniMessage format using XML-like tags for rich text formatting.
   * Supports advanced features like gradients, colors, and click/hover events.
   * Example: "<green>Green text <bold>Bold</bold></green>"
   */
  MINI(MiniMessage.miniMessage()),

  /**
   * Plain text format without any special formatting or color codes.
   * Used for simple text serialization and deserialization.
   * Example: "Plain text without formatting"
   */
  SMART(PlainTextComponentSerializer.plainText());

  /**
   * Serializes a component into a string using this type's serializer.
   *
   * @param component The component to serialize.
   * @return The serialized string representation.
   */
  fun serialize(component: Component): String = serializer.serialize(component)

  /**
   * Deserializes a string into a component using this type's serializer.
   *
   * @param string The string to deserialize.
   * @return The deserialized component.
   */
  fun deserialize(string: String): Component = serializer.deserialize(string)

  companion object {
    /**
     * Determines the appropriate component type based on the content of the string.
     *
     * Detection priority:
     * - LEGACY_SECTION if contains '§'
     * - LEGACY_AMPERSAND if contains '&'
     * - MINI if contains both '<' and '>'
     * - SMART (plain text) otherwise
     *
     * @param content The string content to analyze.
     * @return The detected component type.
     */
    fun get(content: String): ComponentType =
      when {
        content.contains('§') -> LEGACY_SECTION
        content.contains('&') -> LEGACY_AMPERSAND
        content.contains('<') && content.contains('>') -> MINI
        else -> SMART
      }
  }
}

/**
 * Converts this string into a Component by auto-detecting the format type.
 *
 * @return The deserialized component.
 * @see ComponentType.get
 */
fun String.component(): Component = ComponentType.get(this).deserialize(this)

fun String.component(style: Style): Component = component().style(style)

/**
 * Serializes this component into a string using the specified format.
 *
 * @param out The component type to use for serialization. Defaults to LEGACY_AMPERSAND.
 * @return The serialized string representation.
 */
fun Component.text(out: ComponentType = ComponentType.LEGACY_AMPERSAND): String = out.serialize(this)

/**
 * Property accessor for converting this string into a Component.
 *
 * @see component
 */
val String.component: Component get() = component()

/**
 * Property accessor for converting this component into a string using LEGACY_AMPERSAND format.
 *
 * @see text
 */
val Component.text: String get() = text()
