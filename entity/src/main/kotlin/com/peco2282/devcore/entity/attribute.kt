package com.peco2282.devcore.entity

import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity

/**
 * Provides a DSL for modifying entity attributes in a type-safe and convenient way.
 * This file contains utilities for working with Bukkit's Attributable interface.
 */

/**
 * A DSL builder for modifying entity attributes.
 *
 * This class provides operator overloading for getting and setting attribute values
 * in a more idiomatic Kotlin way.
 *
 * @param attributable The attributable entity whose attributes will be modified
 */
@EntityDsl
class AttributeBuilder(private val attributable: Attributable) {
  /**
   * Sets the base value of the specified attribute.
   *
   * @param attribute The attribute to modify
   * @param value The new base value for the attribute
   */
  operator fun set(attribute: Attribute, value: Double) {
    attributable.getAttribute(attribute)?.baseValue = value
  }

  /**
   * Gets the base value of the specified attribute.
   *
   * @param attribute The attribute to retrieve
   * @return The base value of the attribute, or 0.0 if the attribute is not present
   */
  operator fun get(attribute: Attribute): Double {
    return attributable.getAttribute(attribute)?.baseValue ?: 0.0
  }
}

/**
 * Extension function to modify entity attributes using a DSL.
 *
 * This function only works on entities that implement Attributable.
 * If the entity is not Attributable, the editor block is not executed.
 *
 * Example usage:
 * ```
 * entity.attributes {
 *   this[Attribute.GENERIC_MAX_HEALTH] = 20.0
 *   this[Attribute.GENERIC_MOVEMENT_SPEED] = 0.25
 * }
 * ```
 *
 * @param editor A lambda with receiver that configures the entity's attributes
 */
fun Entity.attributes(editor: AttributeBuilder.() -> Unit) {
  if (this is Attributable) {
    AttributeBuilder(this).apply(editor)
  }
}
