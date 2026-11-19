package com.peco2282.devcore.permission

import org.bukkit.permissions.Permissible
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.Plugin

/** A validated permission node together with the Bukkit permission registered for it. */
class TypedPermission internal constructor(
  val node: String,
  val default: PermissionDefault,
  val description: String,
  val children: Map<String, Boolean>
) {
  internal fun bukkit() = Permission(node, description, default, children)
  override fun toString() = node
}

/**
 * DSL body for [permission].
 *
 * ```kotlin
 * val admin = permission("server.admin", PermissionDefault.OP) {
 *   description = "Administration"
 *   child("server.kit.starter", inherit = true)
 * }
 * ```
 */
class PermissionBuilder {
  var description: String = ""
  private val children = linkedMapOf<String, Boolean>()
  fun child(node: String, granted: Boolean = true) { validateNode(node); children[node] = granted }
  fun child(permission: TypedPermission, granted: Boolean = true) = child(permission.node, granted)
  internal fun build(node: String, default: PermissionDefault) = TypedPermission(node, default, description, children.toMap())
}

/** Declares and validates a typed permission without registering it yet. */
fun permission(node: String, default: PermissionDefault = PermissionDefault.FALSE, block: PermissionBuilder.() -> Unit = {}): TypedPermission {
  validateNode(node)
  return PermissionBuilder().apply(block).build(node, default)
}

/** Registers declarations with Bukkit; nodes already present in the manager are left untouched. */
fun Plugin.registerPermissions(vararg permissions: TypedPermission) {
  permissions.forEach { definition ->
    val existing = server.pluginManager.getPermission(definition.node)
    if (existing == null) server.pluginManager.addPermission(definition.bukkit())
  }
}

/** Type-safe equivalent of `hasPermission(permission.node)`. */
fun Permissible.has(permission: TypedPermission): Boolean = hasPermission(permission.node)

private fun validateNode(node: String) {
  require(node.matches(Regex("[a-z0-9._*-]+"))) { "Invalid permission node '$node'" }
}
