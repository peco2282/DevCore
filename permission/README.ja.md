# permission

[English](README.md) | [日本語]

権限ノードの文字列、既定値、説明、子権限を一つの型付き宣言にまとめます。

```kotlin
val ADMIN = permission("server.admin", PermissionDefault.OP) {
  description = "管理機能"
  child("server.kit.starter")
}
plugin.registerPermissions(ADMIN)
if (player.has(ADMIN)) openAdminMenu(player)
```

宣言はプラグイン起動時に登録してください。ノードの書式は宣言時に検証されます。
