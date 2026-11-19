package com.peco2282.testplugin

import com.peco2282.devcore.adventure.openBook
import com.peco2282.devcore.anvilinput.askText
import com.peco2282.devcore.effect.effect
import com.peco2282.devcore.effect.play
import com.peco2282.devcore.effect.textShape
import com.peco2282.devcore.updatechecker.Github
import com.peco2282.devcore.updatechecker.checkForUpdates
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/** End-to-end examples for books, update checks, particle art and anvil text input. */
object PlayerExperiences {
  fun install(plugin: Plugin, updateChecksEnabled: Boolean = false) {
    plugin.checkForUpdates(Github("peco2282/DevCore"), enabled = updateChecksEnabled) { update ->
      if (update.isNewer) plugin.logger.info("DevCore ${update.latest} is available: ${update.url}")
    }
  }

  fun showWelcome(plugin: Plugin, player: Player) {
    player.openBook {
      title("DevCore Guide")
      author("DevCore")
      page {
        append(Component.text("Welcome, ", NamedTextColor.GRAY))
        append(Component.text(player.name, NamedTextColor.AQUA))
        append(Component.text("! Use the anvil prompt to choose a display name.", NamedTextColor.GRAY))
      }
      page { append(Component.text("Particle art is rendered from the same Shape API as circles and helices.", NamedTextColor.GOLD)) }
    }

    player.askText(plugin, title = Component.text("表示名を入力"), initial = player.name) { input ->
      player.displayName(Component.text(input, NamedTextColor.AQUA))
      effect {
        particle(Particle.DUST, textShape(input, scale = 0.06, sampleStep = 3, maxPoints = 300), data =
          Particle.DustOptions(Color.AQUA, 1.0f))
      }.play(player.location.clone().add(0.0, 2.5, 0.0))
    }
  }
}
