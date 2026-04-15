# entity

A module for easy entity management, AI control, and lifecycle handling.

## Usage

### Spawn Entity
Provides a type-safe way to spawn entities using Kotlin DSL.

```kotlin
val zombie = location.spawn<Zombie> {
    isNoAi = true
    isCustomNameVisible = true
    customName = Component.text("Custom Zombie")
}
```

### AI Control
Convenient methods for managing mob AI and goals.

```kotlin
mob.targetNearestPlayer(10.0)
mob.moveTo(targetLocation, 1.2)
mob.clearGoals()
mob.addGoals(1, myCustomGoal)
```

### Lifecycle Management
Automate common entity tasks like periodic actions or delayed removal.

```kotlin
zombie.onTick(plugin, 20.ticks) {
    // Executes every 1 second while entity is valid
    world.spawnParticle(Particle.HAPPY_VILLAGER, location, 5)
}

zombie.onDeath(plugin) {
    player.sendMessage("The zombie died!")
}

zombie.removeAfter(plugin, 60.seconds)
```

## Features
- **Fluent Spawn API**: Spawn and configure entities in a single block.
- **AI Extensions**: Simplified methods for pathfinding, targeting, and goal management.
- **Lifecycle Helpers**: Easily attach tasks and event listeners to specific entities with automatic cleanup.
- **Paper API Support**: Full integration with Paper's MobGoal API.
