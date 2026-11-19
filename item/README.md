# DevCore Item

[English] | [[日本語](README.ja.md)]

Define custom items once: their look, their behavior, and their crafting recipe. Stacks carry a hidden id, so they are
recognized even after a player renames them, and behavior is attached to the item, not to scattered listeners.

## Install (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:item:<version>") }
```

Uses `pdc` (for the hidden id) and `util` (for the item builder).

## Example

```kotlin
val items = plugin.customItems()

val ruby = items.register("ruby_sword", Material.DIAMOND_SWORD) {
  appearance {
    displayName = Component.text("Ruby Sword", NamedTextColor.RED)
    enchant(Enchantment.SHARPNESS, 3)
  }
  onAttack { it.damage *= 1.5 }
  onRightClick { it.player.sendMessage("Whoosh!") }
  shaped(" R", " R", " S") {
    ingredient('R', Material.REDSTONE_BLOCK)
    ingredient('S', Material.STICK)
  }
}

ruby.give(player)            // overflow is dropped at the player's feet
ruby.matches(stack)          // true even if the player renamed it
items.of(stack)              // the CustomItem a stack is, or null
```

## Behavior

All handlers fire only when the item is in the player's **main hand** (or, for consume/drop/place, is the item involved).

| Function | Event |
|---|---|
| `onRightClick` / `onLeftClick` | `PlayerInteractEvent` |
| `onAttack` | `EntityDamageByEntityEvent`, damager is a player |
| `onBreak` | `BlockBreakEvent` |
| `onPlace` | `BlockPlaceEvent` |
| `onConsume` | `PlayerItemConsumeEvent` |
| `onDrop` | `PlayerDropItemEvent` |

- You get the original event, so you can cancel it or change it (`it.isCancelled = true`, `it.damage = ...`).
- An exception in one handler is logged and does not stop the others.
- `placeable = false` stops an item that uses a block material (e.g. `DIRT`) from being placed.

## Recipes

`shaped(rows..) { ingredient(symbol, material) }` and `shapeless { ingredient(material, count) }`. An ingredient can also be
an exact `ItemStack`, for example another custom item: `ingredient('R', ruby.create())`.

Recipes are validated when you register the item (unknown symbol, a symbol with no ingredient, bad shape size).

## Lifecycle

`items.close()` removes the recipes and stops handling events. Stacks that already exist keep their id.
Items from another plugin's registry are never matched by yours.
