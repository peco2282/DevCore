package com.peco2282.devcore.adventure

/**
 * DSL marker annotation for component builder scopes.
 *
 * This annotation is used to prevent implicit receivers from outer scopes
 * being accessible in nested component builder lambdas, ensuring type-safe
 * DSL usage when building Adventure text components.
 */
@DslMarker
annotation class ComponentDsl

/**
 * DSL marker annotation for title builder scopes.
 *
 * This annotation is used to prevent implicit receivers from outer scopes
 * being accessible in nested title builder lambdas, ensuring type-safe
 * DSL usage when configuring Adventure titles.
 */
@DslMarker
annotation class TitleDsl

/**
 * DSL marker annotation for boss-bar builder scopes.
 *
 * This annotation is used to prevent implicit receivers from outer scopes
 * being accessible in nested boss-bar builder lambdas, ensuring type-safe
 * DSL usage when configuring Adventure boss bars.
 */
@DslMarker
annotation class BossBarDsl

/**
 * DSL marker annotation for style builder scopes.
 * 
 * This annotation is used to prevent implicit receivers from outer scopes
 * being accessible in nested style builder lambdas, ensuring type-safe
 * DSL usage when configuring Adventure text component styles.
 */
@DslMarker
annotation class StyleDsl

/**
 * Opt-in annotation for experimental NBT component APIs.
 *
 * This annotation is used to require explicit acceptance of experimental
 * NBT component operations, indicating that the marked Adventure APIs
 * may change in future releases.
 */
@RequiresOptIn("This function is experimental and may change in the future.")
annotation class ExperimentalNbtComponent
