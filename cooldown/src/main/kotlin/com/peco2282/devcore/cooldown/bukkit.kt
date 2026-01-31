package com.peco2282.devcore.cooldown

import org.bukkit.entity.Player
import java.util.UUID
import kotlin.time.Duration

typealias PlayerCooldowns = Cooldowns<UUID>
typealias PlayerDebounce = Debounce<UUID>

/**
 * Tries to use the cooldown for the specified [player] with the given [cooldown].
 */
fun PlayerCooldowns.tryUse(player: Player, cooldown: Duration): Boolean = tryUse(player.uniqueId, cooldown)

/**
 * Tries to allow an action for the specified [player] with the given [minInterval].
 */
fun PlayerDebounce.allowEvery(player: Player, minInterval: Duration): Boolean = allowEvery(player.uniqueId, minInterval)

