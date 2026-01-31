package com.peco2282.devcore.cooldown

import org.bukkit.entity.Player
import java.util.UUID
import kotlin.time.Duration

typealias PlayerCooldowns = Cooldowns<UUID>
typealias PlayerDebounce = Debounce<UUID>

fun PlayerCooldowns.tryUse(player: Player, cooldown: Duration): Boolean = tryUse(player.uniqueId, cooldown)
fun PlayerDebounce.allowEvery(player: Player, minInterval: Duration): Boolean = allowEvery(player.uniqueId, minInterval)

