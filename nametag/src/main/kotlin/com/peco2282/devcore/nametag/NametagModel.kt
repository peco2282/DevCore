package com.peco2282.devcore.nametag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID

/** Visual and sorting properties for a player name. */
class NametagConfig {
  var prefix: Component = Component.empty()
  var suffix: Component = Component.empty()
  var color: NamedTextColor = NamedTextColor.WHITE
  var sortKey: String = "50"
}

internal fun teamName(sortKey: String, playerId: UUID): String {
  require(sortKey.matches(Regex("[a-zA-Z0-9._-]{1,8}"))) {
    "sortKey must contain 1..8 ASCII letters, digits, '.', '_' or '-'"
  }
  return sortKey.padEnd(8, '~') + playerId.toString().filter(Char::isLetterOrDigit).take(8)
}

internal fun applyNametag(scoreboard: Scoreboard, playerId: UUID, entry: String, config: NametagConfig) {
  scoreboard.getEntryTeam(entry)?.let { old ->
    if (old.name.startsWith(config.sortKey.padEnd(8, '~'))) return@let
    old.removeEntry(entry)
    if (old.entries.isEmpty()) old.unregister()
  }
  val name = teamName(config.sortKey, playerId)
  val team = scoreboard.getTeam(name) ?: scoreboard.registerNewTeam(name)
  team.prefix(config.prefix)
  team.suffix(config.suffix)
  team.color(config.color)
  team.addEntry(entry)
}
