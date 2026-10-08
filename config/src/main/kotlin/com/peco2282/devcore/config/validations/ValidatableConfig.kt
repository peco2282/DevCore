package com.peco2282.devcore.config.validations

/**
 * Provides validation that depends on more than one configuration property.
 *
 * Implement this interface when individual property annotations cannot express
 * an invariant, such as a minimum value not exceeding a maximum value.
 */
interface ValidatableConfig {

  /**
   * Validates this configuration object.
   *
   * @throws IllegalArgumentException when the configuration is invalid
   */
  fun validate()
}
