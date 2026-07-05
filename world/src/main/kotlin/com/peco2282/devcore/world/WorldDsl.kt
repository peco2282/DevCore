package com.peco2282.devcore.world


/**
 * DSL marker annotation for world-related builder patterns.
 * 
 * This annotation is used to create type-safe builders for world configuration
 * and manipulation. It restricts implicit receivers in nested DSL scopes,
 * preventing accidental access to outer scope functions.
 */
@DslMarker
annotation class WorldDsl

/**
 * Represents different weather conditions that can be applied to a world.
 * 
 * This enum defines the available weather states for world configuration.
 */
enum class WeatherType {
  /**
   * Clear weather with no precipitation.
   * The sky is clear and sunny.
   */
  CLEAR,

  /**
   * Rainy weather with precipitation.
   * Water particles fall from the sky.
   */
  RAIN,

  /**
   * Thunderstorm weather with lightning and rain.
   * Includes both precipitation and periodic lightning strikes.
   */
  THUNDER
}
