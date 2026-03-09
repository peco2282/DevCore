package com.peco2282.devcore.config.validations.annotations

/**
 * Adds a comment to the configuration property.
 *
 * @property text the text of the comment to be added above the property in the YAML file
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Comment(val text: String)

/**
 * Validates that the property value is within the specified [min] and [max] range.
 *
 * @property min the minimum allowed value (inclusive)
 * @property max the maximum allowed value (inclusive)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Range(
  val min: Long = Long.MIN_VALUE,
  val max: Long = Long.MAX_VALUE
)

/**
 * Validates that the string property value is not blank.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlank

/**
 * Validates that the size of the collection property is within the specified [min] and [max] range.
 *
 * @property min the minimum allowed size (inclusive)
 * @property max the maximum allowed size (inclusive)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Size(
  val min: Int = 0,
  val max: Int = Int.MAX_VALUE
)

/**
 * Validates that the string property value matches the specified regex [pattern].
 *
 * @property pattern the regex pattern that the value must match
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Regex(
  val pattern: String
)

/**
 * Validates that the numeric property value is at least [value].
 *
 * @property value the minimum allowed value (inclusive)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Min(val value: Long)

/**
 * Validates that the numeric property value is at most [value].
 *
 * @property value the maximum allowed value (inclusive)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Max(val value: Long)

/**
 * Validates that the numeric property value is positive (greater than zero).
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Positive

/**
 * Validates that the string property value is a valid URL.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class URL

/**
 * Validates that the file path represented by the string property exists on the file system.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FileExists
