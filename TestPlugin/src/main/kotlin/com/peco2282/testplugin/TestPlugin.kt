package com.peco2282.testplugin

import com.peco2282.devcore.command.command
import com.peco2282.devcore.config.Configs
import com.peco2282.devcore.config.reflection.TypeSerializers
import com.peco2282.devcore.config.serializers.ComponentSerializer
import net.kyori.adventure.text.Component
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {
    companion object {
        private lateinit var plugin: TestPlugin
        val instance by lazy { plugin }
        lateinit var pluginConfig: Config
    }

    override fun onEnable() {
        plugin = this
        // Plugin startup logic

        TypeSerializers.register(Component::class, ComponentSerializer())

        saveDefaultConfig()
        pluginConfig = Configs.load(this)
        Configs.save(this, pluginConfig)

        @Suppress("UnusedExpression")
        EventListener

        command {
            literal("cmd") {
                requireOp()
                executesPlayer { player, _ ->
                    player.sendMessage(pluginConfig.message)
//                    player.sendMessage(pluginConfig.formattedMessage)
                    1
                }
            }
            literal("reload") {
                requireOp()
                executesPlayer { player, context ->
                    pluginConfig = Configs.load(this@TestPlugin)
                    player.sendMessage(Component.text("Config reloaded!"))
                    1
                }
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
