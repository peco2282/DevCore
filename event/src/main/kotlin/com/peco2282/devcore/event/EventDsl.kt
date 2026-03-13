package com.peco2282.devcore.event

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import java.util.logging.Level

/**
 * イベントハンドリングのためのDSLビルダー。
 */
class EventBuilder<T : Event>(
    val plugin: Plugin,
    val type: Class<T>,
    var priority: EventPriority = EventPriority.NORMAL,
    var ignoreCancelled: Boolean = false
) {
    private val filters = mutableListOf<T.() -> Boolean>()
    private var handler: (T.() -> Unit)? = null
    private var once = false
    private var debug = false
    private var registeredListener: RegisteredListener? = null
    private var isRegistered = false

    /**
     * デバッグモードを有効にします。
     */
    fun debug(): EventBuilder<T> = apply {
        debug = true
    }

    /**
     * イベントが実行される条件を追加します。
     */
    fun filter(predicate: T.() -> Boolean): EventBuilder<T> {
        filters.add(predicate)
        return this
    }

    /**
     * 1回だけ実行されるように設定します。
     */
    fun once(): EventBuilder<T> = apply {
        once = true
    }

    /**
     * 特定の回数だけ実行されるように設定します。
     */
    fun take(times: Int): EventBuilder<T> = apply {
        var count = 0
        filter {
            count++
            if (count >= times) unregister()
            count <= times
        }
    }

    /**
     * 条件を満たすまでリスナーを維持します。
     */
    fun takeWhile(predicate: T.() -> Boolean): EventBuilder<T> = filter {
        val result = predicate()
        if (!result) unregister()
        result
    }

    /**
     * イベントがPlayerEventを継承している場合、特定のプレイヤーに対してのみ実行されるようにします。
     */
    fun filterPlayer(predicate: org.bukkit.entity.Player.() -> Boolean): EventBuilder<T> = filter {
        if (this is PlayerEvent) {
            predicate(player)
        } else {
            true
        }
    }

    /**
     * イベントをキャンセルします（Cancellableなイベントの場合のみ）。
     */
    fun T.cancel() {
        if (this is Cancellable) {
            isCancelled = true
        }
    }

    /**
     * イベントを指定された秒数後に登録解除します。
     */
    fun expireAfter(seconds: Long): EventBuilder<T> = apply {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            unregister()
        }, seconds * 20)
    }

    /**
     * イベント実行時の処理を定義します。
     */
    fun handle(action: T.() -> Unit): EventBuilder<T> = apply {
        this.handler = action
        register()
    }

    /**
     * リスナーを解除します。
     */
    fun unregister() {
        if (!isRegistered) return
        val handlers = getHandlerList(type)
        registeredListener?.let { handlers.unregister(it) }
        isRegistered = false
    }

    private fun register() {
        if (isRegistered) return
        val listener = object : Listener {}
        val executor = { _: Listener, event: Event ->
            if (type.isInstance(event)) {
                @Suppress("UNCHECKED_CAST")
                val e = event as T
                if (debug) {
                    plugin.logger.log(Level.INFO, "[EventDSL Debug] Handling event: ${type.simpleName}")
                }
                if (filters.all { it(e) }) {
                    handler?.invoke(e)
                    if (once) {
                        unregister()
                    }
                } else if (debug) {
                    plugin.logger.log(Level.INFO, "[EventDSL Debug] Event filtered: ${type.simpleName}")
                }
            }
        }

        Bukkit.getPluginManager().registerEvent(
            type,
            listener,
            priority,
            executor,
            plugin,
            ignoreCancelled
        )

        // 登録されたRegisteredListenerを特定して保持する（unregisterのため）
        val handlers = getHandlerList(type)
        registeredListener = handlers.registeredListeners.find { it.listener === listener }
        isRegistered = true
    }

    private fun getHandlerList(clazz: Class<out Event>): HandlerList {
        return try {
            val method = clazz.getMethod("getHandlerList")
            method.invoke(null) as HandlerList
        } catch (e: Exception) {
            // イベントクラス自体にgetHandlerListがない場合、親クラスを探す（一般的ではないがBukkitの仕様上ありえる）
            clazz.methods.find { it.name == "getHandlerList" }?.invoke(null) as? HandlerList
                ?: throw IllegalStateException("Event ${clazz.name} does not have getHandlerList() method.")
        }
    }
}

/**
 * イベントリスナーをDSLで定義します。
 */
inline fun <reified T : Event> Plugin.on(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline block: EventBuilder<T>.() -> Unit
): EventBuilder<T> {
    return EventBuilder(this, T::class.java, priority, ignoreCancelled).apply(block)
}

/**
 * 1回だけ実行されるイベントリスナーを定義します。
 */
inline fun <reified T : Event> Plugin.listenOnce(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline action: T.() -> Unit
): EventBuilder<T> {
    return on(priority, ignoreCancelled) {
        once()
        handle(action)
    }
}

/**
 * 複数のイベントをまとめて管理するためのクラス。
 */
class EventGroup(val plugin: Plugin) {
    val builders = mutableListOf<EventBuilder<*>>()

    /**
     * イベントを登録します。
     */
    inline fun <reified T : Event> on(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline block: EventBuilder<T>.() -> Unit
    ): EventBuilder<T> {
        return EventBuilder(plugin, T::class.java, priority, ignoreCancelled).apply(block).also {
            builders.add(it)
        }
    }

    /**
     * 1回だけ実行されるイベントを登録します。
     */
    inline fun <reified T : Event> listenOnce(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline action: T.() -> Unit
    ): EventBuilder<T> {
        return on(priority, ignoreCancelled) {
            once()
            handle(action)
        }
    }

    /**
     * 全てのイベントリスナーを解除します。
     */
    fun unregisterAll() {
        builders.forEach { it.unregister() }
        builders.clear()
    }
}

/**
 * 複数のイベントリスナーをまとめて定義します。
 */
fun Plugin.events(block: EventGroup.() -> Unit): EventGroup {
    return EventGroup(this).apply(block)
}
