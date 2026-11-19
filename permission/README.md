# permission

[English] | [日本語](README.ja.md)

Typed permission declarations that keep node spelling, defaults, descriptions and children together.

```kotlin
val ADMIN = permission("server.admin", PermissionDefault.OP) {
  description = "Server administration"
  child("server.kit.starter", inherit = true)
}
val STARTER = permission("server.kit.starter", PermissionDefault.TRUE)

plugin.registerPermissions(STARTER, ADMIN)
if (player.has(ADMIN)) openAdminMenu(player)
```

Register declarations during plugin startup. Duplicate registrations are ignored only when Bukkit already has that node; node syntax is validated when declared.
