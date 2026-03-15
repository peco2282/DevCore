package com.peco2282.testplugin

import com.peco2282.devcore.command.command
import com.peco2282.devcore.config.Configs
import com.peco2282.devcore.config.getConfigInstance
import com.peco2282.devcore.config.reflection.TypeSerializers
import com.peco2282.devcore.config.serializers.ComponentSerializer
import com.peco2282.devcore.cooldown.Cooldowns
import com.peco2282.devcore.gui.fill
import com.peco2282.devcore.gui.gui
import com.peco2282.devcore.packet.sendFakeVisuals
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.peco2282.devcore.gui.GuiListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration.Companion.seconds

class TestPlugin : JavaPlugin() {
  companion object {
    private lateinit var plugin: TestPlugin
    val instance by lazy { plugin }
    lateinit var pluginConfig: Config
    val cooldowns = Cooldowns<String>()
  }

  override fun onEnable() {
    plugin = this
    // Plugin startup logic

    GuiListener.register(this)
    TypeSerializers.register(Component::class, ComponentSerializer())

    saveDefaultConfig()
    pluginConfig = getConfigInstance()
    Configs.save(this, pluginConfig)

    @Suppress("UnusedExpression")
    EventListener

    command {
      literal("cmd") {
        requireOp()
        executesPlayer { player, _ ->
          player.sendMessage(pluginConfig.message)
          player.sendMessage(pluginConfig.formattedMessage)
          1
        }
      }
      literal("reload") {
        requireOp()
        executesPlayer { player, _ ->
          try {
            pluginConfig = Configs.load(this@TestPlugin)
            player.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN))
          } catch (e: Exception) {
            player.sendMessage(Component.text("Failed to reload config: ${e.message}", NamedTextColor.RED))
          }
          1
        }
      }
      literal("fakeentity") {
        requireOp()
        executesPlayer { player, _ ->
          player.sendFakeVisuals {
            spawnEntity(EntityTypes.ZOMBIE, player.location.add(player.location.direction.multiply(3))) {
              customName = "Fake Zombie"
              isGlowing = true
              equipment {
                helmet = org.bukkit.inventory.ItemStack(Material.DIAMOND_HELMET)
              }
            }
          }
          player.sendMessage(Component.text("Spawned a fake zombie!"))
          1
        }
      }
      literal("cooldown") {
        requireOp()
        executesPlayer { player, _ ->
          if (cooldowns.tryUse(player.name, 10.seconds)) {
            player.sendMessage(Component.text("Action performed!"))
          } else {
            val remaining = cooldowns.remainingMillis(player.name) / 1000.0
            player.sendMessage(Component.text("Please wait %.1f seconds.".format(remaining)))
          }
          1
        }
      }
      literal("gui") {
        requireOp()
        executesPlayer { player, _ ->
          val inventory = gui(Component.text("Test GUI"), 3) {
            fill(Material.GRAY_STAINED_GLASS_PANE) {
              keep()
            }
            slot(13) {
              icon(Material.APPLE)
              name(Component.text("Click Me!", NamedTextColor.GOLD))
              onClick {
                player.sendMessage(Component.text("You clicked the apple!", NamedTextColor.GREEN))
                player.closeInventory()
              }
            }
          }
          player.openInventory(inventory)
          1
        }
      }
      literal("validate") {
        requireOp()
        executesPlayer { player, _ ->
          // Range(0, 100) なので 200 をセットしてバリデーションエラーを誘発させる
          val file = dataFolder.resolve("config.yml")
          val content = file.readText()
          val updatedContent = content.replace("count: 0", "count: 200")
          file.writeText(updatedContent)
          player.sendMessage(Component.text("Config modified with invalid value. Try /reload!"))
          1
        }
      }
    }
  }

  override fun onDisable() {
    // Plugin shutdown logic
  }
}
