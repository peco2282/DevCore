# DevCore PDC

[English] | [[日本語](README.ja.md)]

Typed access to Bukkit's `PersistentDataContainer`: no `NamespacedKey` + `PersistentDataType` pairs to keep in sync,
and properties that save themselves.

## Install (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:pdc:<version>") }
```

## Typed keys

The key carries its value type, so a read cannot disagree with a write.

```kotlin
val KILLS = pdcKey<Int>(plugin, "kills")
val OWNER = pdcKey<UUID>(plugin, "owner")

player[KILLS] = 3                       // any PersistentDataHolder: Player, Entity, Chunk, World, tile entities
val kills: Int? = player[KILLS]
if (KILLS in player) player.remove(KILLS)

player.persistentDataContainer.update(KILLS, default = 0) { it + 1 }   // read-modify-write
container.getOrPut(OWNER) { player.uniqueId }
```

Supported out of the box: `String`, `Int`, `Long`, `Double`, `Float`, `Short`, `Byte`, `Boolean`,
`ByteArray`, `IntArray`, `LongArray`, `UUID`, any `enum`, and `List<T>` of the primitive types.
Anything else fails when the key is created, not at some later read.

```kotlin
PdcTypes.registerString(Color3::class, { "${it.r},${it.g},${it.b}" }, { /* parse */ })
val TINT = pdcKey<Color3>(plugin, "tint")
```

## Properties that persist

```kotlin
var Player.kills: Int by pdc(plugin, "kills", default = 0)
var Player.nickname: String? by pdcOrNull(plugin, "nickname")   // assigning null removes it

player.kills++
```

Declare these as extension properties at the top level. When you have no plugin instance at that point, pass a
`NamespacedKey` instead: `pdc(NamespacedKey.fromString("myplugin:kills")!!, 0)`.

## Items and blocks

`ItemMeta` is a copy, so writing to an item's container normally needs a get, a change and a set. Not here:

```kotlin
item[OWNER] = player.uniqueId
item.editPdc { this[OWNER] = id; this[KILLS] = 0 }       // several writes, one meta update
var ItemStack.level: Int by itemPdc(plugin, "level", 1)

block.editPdc { this[OWNER] = id }                       // chests, signs, furnaces... returns false for plain blocks
```

Plain blocks (stone, dirt) cannot hold data. Store it on the chunk, keyed by position.
