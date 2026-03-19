# DevCore Packet

[English] | [[日本語](README.ja.md)]

DSL for handling fake entities and packets using PacketEvents.

## Features

- Easy-to-use DSL for spawning fake entities
- Support for fake block changes
- Integration with PacketEvents for high-performance packet handling

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:packet:<version>")
}
```

## Usage

### Fake Entity DSL

```kotlin
player.sendFakeVisuals {
  spawnEntity(EntityType.ZOMBIE, location) {
    customName = "Fake Boss"
    isGlowing = true
    equipment {
      helmet = ItemStack(Material.GOLDEN_HELMET)
    }
  }
}
```

### Fake Blocks

```kotlin
player.sendFakeVisuals {
  setFakeBlock(location.add(0.0, 1.0, 0.0), Material.DIAMOND_BLOCK)
}
```
