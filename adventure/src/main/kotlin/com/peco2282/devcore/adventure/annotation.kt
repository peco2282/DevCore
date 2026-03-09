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
 * DSL marker annotation for style builder scopes.
 * 
 * This annotation is used to prevent implicit receivers from outer scopes
 * being accessible in nested style builder lambdas, ensuring type-safe
 * DSL usage when configuring Adventure text component styles.
 */
@DslMarker
annotation class StyleDsl
