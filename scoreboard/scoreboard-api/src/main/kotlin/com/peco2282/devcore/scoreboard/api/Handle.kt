package com.peco2282.devcore.scoreboard.api

import org.bukkit.entity.Player

/**
 * Represents a generic handle for managing display elements like scoreboards or boss bars.
 */
interface Handle {
  /**
   * Updates the state of the scoreboard or boss bar.
   */
  fun update()

  /**
   * Shows the display element to the specified player.
   *
   * @param player The player to show the element to.
   */
  fun show(player: Player)

  /**
   * Hides the display element from the specified player.
   *
   * @param player The player to hide the element from.
   */
  fun hide(player: Player)

  /**
   * Hides the display element from all players and releases associated resources.
   */
  fun destroy()
}

/**
 * A specialized handle for managing sidebars (scoreboards).
 */
interface SidebarHandle : Handle

/**
 * A specialized handle for managing boss bars.
 */
interface BossBarHandle : Handle
