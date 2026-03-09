package com.peco2282.testplugin

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
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
