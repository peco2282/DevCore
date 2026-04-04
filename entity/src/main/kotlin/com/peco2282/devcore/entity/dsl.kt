package com.peco2282.devcore.entity

/**
 * DSL marker annotation for the entity DSL.
 *
 * Applying this annotation prevents implicit receiver leaking between nested DSL scopes,
 * ensuring that only the intended builder methods are accessible within each block.
 */
@DslMarker
annotation class EntityDsl
