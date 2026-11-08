@file:JvmMultifileClass
@file:JvmName("HelperKt")

package com.peco2282.devcore.util

/**
 * Marks declarations that are internal to DevCore and should not be used by external consumers.
 *
 * APIs marked with this annotation may change or be removed without warning in minor or patch releases.
 */
@RequiresOptIn("This API is internal to DevCore and may change without a major version bump.")
annotation class DevCoreInternal
