package com.peco2282.testplugin

import com.peco2282.devcore.command.command
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {
    companion object {
        private lateinit var plugin: TestPlugin
        val instance by lazy { plugin }
    }

    override fun onEnable() {
        plugin = this
        // Plugin startup logic

        @Suppress("UnusedExpression")
        EventListener

        command("testplugin") {
            literal("cmd") {
                requires { it.sender.isOp }
                executes {
                    it.source.sender.sendMessage("Hello, World!")
                    1
                }
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
