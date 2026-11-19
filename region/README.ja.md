# region

[English](README.md) | [日本語]

直方体・球・円柱の領域判定と、進入・退出の追跡を提供します。

```kotlin
val spawn = cuboid(world, first, second)
plugin.regions().register("spawn", spawn) {
  onEnter { player.sendMessage("ようこそ") }
  onLeave { player.sendMessage("さようなら") }
}
```

各領域は1つのワールドに属し、境界値は自動的に正規化されます。レジストリは移動・テレポート時に所属状態を比較します。不要になった領域は登録解除してください。
