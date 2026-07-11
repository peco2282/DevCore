package com.peco2282.testplugin

import com.peco2282.devcore.adventure.send
import com.peco2282.devcore.adventure.text
import com.peco2282.devcore.command.*
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
      literal("argument") {
        requireOp()
        literal("integer") {
          integer("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Int::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("double") {
          double("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Double::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("float") {
          float("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Float::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("long") {
          long("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Long::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("boolean") {
          boolean("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Boolean::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("string") {
          string("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", String::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("greedyString") {
          greedyString("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", String::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("word") {
          word("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", String::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("team") {
          team("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.scoreboard.Team::class.java)
              player.sendMessage(Component.text("Value: ${value.name}"))
              1
            }
          }
        }
        literal("slot") {
          slot("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Int::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("slots") {
          slots("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", com.peco2282.devcore.command.argument.SlotRange::class.java)
              player.sendMessage(Component.text("Value: ${value.serializedName} (${value.slots})"))
              1
            }
          }
        }
        literal("objective") {
          objective("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.scoreboard.Objective::class.java)
              player.sendMessage(Component.text("Value: ${value.name}"))
              1
            }
          }
        }
        literal("entity") {
          entity("val") {
            executesPlayer { player, context ->
              // Note: Result type of entity() is EntitySelectorArgumentResolver, but usually we use context.getArgument for simpler access if possible, or resolver.
              // For simplicity in TestPlugin, we just confirm it executes.
              player.sendMessage(Component.text("Entity argument accepted"))
              1
            }
          }
        }
        literal("player") {
          player("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("Player argument accepted"))
              1
            }
          }
        }
        literal("blockPosition") {
          blockPos("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("BlockPosition argument accepted"))
              1
            }
          }
        }
        literal("columnBlockPosition") {
          columnBlockPos("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("ColumnBlockPosition argument accepted"))
              1
            }
          }
        }
        literal("finePosition") {
          finePos("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("FinePosition argument accepted"))
              1
            }
          }
        }
        literal("rotation") {
          rotation("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("Rotation argument accepted"))
              1
            }
          }
        }
        literal("angle") {
          angle("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("Angle argument accepted"))
              1
            }
          }
        }
        literal("axes") {
          axes("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Set::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("blockState") {
          blockState("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.block.BlockState::class.java)
              player.sendMessage(Component.text("Value: ${value.type}"))
              1
            }
          }
        }
        literal("itemStack") {
          itemStack("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", ItemStack::class.java)
              player.sendMessage(Component.text("Value: ${value.type}"))
              1
            }
          }
        }
        literal("namedColor") {
          namedColor("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", net.kyori.adventure.text.format.NamedTextColor::class.java)
              player.sendMessage(Component.text("Value: $value", value))
              1
            }
          }
        }
        literal("component") {
          component("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Component::class.java)
              player.sendMessage(Component.text("Value: ").append(value))
              1
            }
          }
        }
        literal("style") {
          style("val") {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("Style argument accepted"))
              1
            }
          }
        }
        literal("scoreboardDisplaySlot") {
          scoreboardDisplaySlot("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.scoreboard.DisplaySlot::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("namespacedKey") {
          namespacedKey("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.NamespacedKey::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("key") {
          key("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", net.kyori.adventure.key.Key::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("world") {
          world("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.World::class.java)
              player.sendMessage(Component.text("Value: ${value.name}"))
              1
            }
          }
        }
        literal("gameMode") {
          gameMode("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.GameMode::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("heightMap") {
          heightMap("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.HeightMap::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("uuid") {
          uuid("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", java.util.UUID::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("objectiveCriteria") {
          objectiveCriteria("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.scoreboard.Criteria::class.java)
              player.sendMessage(Component.text("Value: ${value.name}"))
              1
            }
          }
        }
        literal("entityAnchor") {
          entityAnchor("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", io.papermc.paper.entity.LookAnchor::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("time") {
          time("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", Int::class.java)
              player.sendMessage(Component.text("Value: $value ticks"))
              1
            }
          }
        }
        literal("templateMirror") {
          templateMirror("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.block.structure.Mirror::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("templateRotation") {
          templateRotation("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.block.structure.StructureRotation::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("enchantment") {
          enchantment("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.enchantments.Enchantment::class.java)
              player.sendMessage(Component.text("Value: ${value.key}"))
              1
            }
          }
        }
        literal("potionEffectType") {
          potionEffectType("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.potion.PotionEffectType::class.java)
              player.sendMessage(Component.text("Value: ${value.key}"))
              1
            }
          }
        }
        literal("material") {
          material("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.Material::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("particle") {
          particle("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.Particle::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("attribute") {
          attribute("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.attribute.Attribute::class.java)
              player.sendMessage(Component.text("Value: $value"))
              1
            }
          }
        }
        literal("dimension") {
          dimension("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.World::class.java)
              player.sendMessage(Component.text("Value: ${value.name}"))
              1
            }
          }
        }
        literal("advancement") {
          advancement("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.advancement.Advancement::class.java)
              player.sendMessage(Component.text("Value: ${value.key}"))
              1
            }
          }
        }
        literal("lootTable") {
          lootTable("val") {
            executesPlayer { player, context ->
              val value = context.getArgument("val", org.bukkit.loot.LootTable::class.java)
              player.sendMessage(Component.text("Value: ${value.key}"))
              1
            }
          }
        }
        literal("resourceKey") {
          resourceKey("val", io.papermc.paper.registry.RegistryKey.ITEM) {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("ResourceKey argument accepted"))
              1
            }
          }
        }
        literal("resource") {
          resource("val", io.papermc.paper.registry.RegistryKey.ITEM) {
            executesPlayer { player, context ->
              player.sendMessage(Component.text("Resource argument accepted"))
              1
            }
          }
        }
        literal("blockInWorldPredicate") {
          blockInWorldPredicate("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("blockInWorldPredicate argument accepted"))
              1
            }
          }
        }
        literal("itemStackPredicate") {
          itemStackPredicate("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("itemStackPredicate argument accepted"))
              1
            }
          }
        }
        literal("integerRange") {
          integerRange("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("integerRange argument accepted"))
              1
            }
          }
        }
        literal("doubleRange") {
          doubleRange("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("doubleRange argument accepted"))
              1
            }
          }
        }
        literal("playerProfiles") {
          playerProfiles("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("playerProfiles argument accepted"))
              1
            }
          }
        }
        literal("signedMessage") {
          signedMessage("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("signedMessage argument accepted"))
              1
            }
          }
        }
        literal("columnFinePosition") {
          columnFinePos("val") {
            executesPlayer { player, _ ->
              player.sendMessage(Component.text("columnFinePosition argument accepted"))
              1
            }
          }
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

      literal("argument") {
        literal("team") {
          team("teamName") {
            executesPlayer { player, ctx ->
              val team = ctx.getTeam("teamName")
              player.sendMessage("Selected team: ${team?.name}")
              1
            }
          }
        }
        literal("player") {
          player("target") {
            executesPlayer { player, ctx ->
              val target = ctx.getPlayer("target")
              player.sendMessage("Selected player: ${target?.name}")
              1
            }
          }
        }
        literal("integer") {
          integer("value", min = 0, max = 100) {
            executesPlayer { player, ctx ->
              val value = ctx.getArg<Int>("value")
              player.sendMessage("Selected value: $value")
              1
            }
          }
        }
        literal("item") {
          itemStack("item") {
            executesPlayer { player, ctx ->
              val item = ctx.getItemStack("item")
              player.inventory.addItem(item)
              player.sendMessage(Component.text("Gave you: ").append(item.displayName()))
              1
            }
          }
        }
        literal("gamemode") {
          gameMode("mode") {
            executesPlayer { player, ctx ->
              val mode = ctx.getGameMode("mode")
              player.gameMode = mode
              player.sendMessage("Set gamemode to: $mode")
              1
            }
          }
        }
        literal("blockpos") {
          blockPos("pos") {
            executesPlayer { player, ctx ->
              val pos = ctx.getLocation("pos")
              player.sendMessage("Selected block position: ${pos.blockX}, ${pos.blockY}, ${pos.blockZ}")
              1
            }
          }
        }
        literal("color") {
          namedColor("color") {
            executesPlayer { player, ctx ->
              val color = ctx.getNamedColor("color")
              player.sendMessage(Component.text("Selected color: $color").color(color))
              1
            }
          }
        }
        literal("advancement") {
          advancement("advancement") {
            executesPlayer { player, ctx ->
              val advancement = ctx.getAdvancement("advancement")
              player.sendMessage("Selected advancement: ${advancement?.display?.title()?.text}")
              1
            }
          }
        }
      }
    }
  }

  override fun onDisable() {
    // Plugin shutdown logic
  }
}
