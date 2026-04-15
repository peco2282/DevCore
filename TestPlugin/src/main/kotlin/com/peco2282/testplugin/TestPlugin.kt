package com.peco2282.testplugin

import com.peco2282.devcore.adventure.send
import com.peco2282.devcore.command.command
import com.peco2282.devcore.command.getWorld
import com.peco2282.devcore.config.Configs
import com.peco2282.devcore.config.getConfigInstance
import com.peco2282.devcore.config.reflection.TypeSerializers
import com.peco2282.devcore.config.serializers.ComponentSerializer
import com.peco2282.devcore.cooldown.Cooldowns
import com.peco2282.devcore.entity.*
import com.peco2282.devcore.gui.GuiListener
import com.peco2282.devcore.gui.fill
import com.peco2282.devcore.gui.gui
import com.peco2282.devcore.packet.EntityAnimation
import com.peco2282.devcore.packet.Packets
import com.peco2282.devcore.packet.onPacket
import com.peco2282.devcore.packet.packet
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.world.edit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
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

    Packets.init(this)
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
      literal("packet") {
        requireOp()
        literal("title") {
          executesPlayer { player, _ ->
            packet(player) {
              title {
                title = "Packet Title"
                subtitle = "Subtitle here"
                fadeIn = 10
                stay = 40
                fadeOut = 10
              }
            }
            1
          }
        }
        literal("actionbar") {
          executesPlayer { player, _ ->
            packet(player) {
              actionBar("§aAction Bar from Packet DSL")
            }
            1
          }
        }
        literal("sound") {
          executesPlayer { player, _ ->
            packet(player) {
              sound {
                type = Sound.ENTITY_EXPERIENCE_ORB_PICKUP
                volume = 1f
                pitch = 2f
              }
            }
            1
          }
        }
        literal("listen") {
          executesPlayer { player, _ ->
            player.sendMessage("§aStarted listening to packets (any packet)...")
            player.onPacket<Any> { packet ->
              if (packet::class.java.simpleName.contains("Chat", ignoreCase = true)) {
                player.sendMessage("§7[PacketLog] Chat-related packet detected: ${packet::class.java.simpleName}")
              }
            }
            1
          }
        }
        literal("fakeentity") {
          executesPlayer { player, _ ->
            packet(player) {
              sendFakeEntity(EntityType.ZOMBIE, player.location) {
                customName = "§cDSL Zombie"
                isCustomNameVisible = true
                isGlowing = true
                equipment {
                  helmet = ItemStack(Material.DIAMOND_HELMET)
                  mainHand = ItemStack(Material.DIAMOND_SWORD)
                }
                animate(EntityAnimation.SWING_MAIN_HAND)
                despawnAfter(100L) // 5 seconds
              }
            }
            player.sendMessage("§aFake Zombie spawned for 5 seconds!")
            1
          }
        }
        literal("particles") {
          executesPlayer { player, _ ->
            packet(player) {
              particles(Particle.FLAME) {
                amount = 100
                offset = Vector(1.0, 1.0, 1.0)
                extra = 0.05
              }
            }
            player.sendMessage("§aFlame particles sent!")
            1
          }
        }
        literal("fakeblocks") {
          executesPlayer { player, _ ->
            packet(player) {
              fakeBlocks {
                fill(player.location.add(2.0, 0.0, 2.0), player.location.add(4.0, 2.0, 4.0), Material.GOLD_BLOCK)
              }
            }
            player.sendMessage("§aFake gold blocks created!")
            1
          }
        }
        literal("camera") {
          executesPlayer { player, _ ->
            packet(player) {
              // Set camera to self (just for test, normally use an entity id)
              camera(player.entityId)
            }
            player.sendMessage("§aCamera set to self!")
            1
          }
        }
        literal("border") {
          executesPlayer { player, _ ->
            packet(player) {
              worldBorder {
                center(player.location)
                size = 10.0
                warningDistance = 2
              }
            }
            player.sendMessage("§aPersonal world border set!")
            1
          }
        }
        literal("sign") {
          executesPlayer { player, _ ->
            packet(player) {
              openSign(player.location.block.location)
            }
            player.sendMessage("§aOpening sign editor at your feet (needs a sign block there!)")
            1
          }
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
      literal("world") {
        world("worldName") {
          executesPlayer { player, ctx ->
            val world = ctx.getWorld("worldName")
            world.edit {
            }
            player.sendMessage(Component.text("You are in world: ${player.world.name}"))
            1
          }
        }
      }
      literal("entity") {
        requireOp()
        executesPlayer { player, _ ->
          player.location.spawn<Zombie> {
            this.isNoAi = true
            this.onDeath(this@TestPlugin) {
              player.send {
                text("The dummy zombie died!") {
                  red()
                }
              }
            }
            this.onTick(this@TestPlugin, 20.ticks) {
              this.targetNearestPlayer(10.0)
            }
            this.removeAfter(this@TestPlugin, 60.seconds)
          }
          player.send {
            text("Spawned a dummy zombie with custom AI/Lifecycle!") {
              green()
            }
          }
          1
        }
      }
    }
  }

  override fun onDisable() {
    // Plugin shutdown logic
  }
}
