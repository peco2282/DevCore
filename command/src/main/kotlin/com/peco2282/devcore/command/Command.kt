@file:Suppress("UnstableApiUsage")

package com.peco2282.devcore.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.peco2282.devcore.adventure.builder.ComponentBuilder
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.command.argument.DevCoreArgumentTypes
import com.peco2282.devcore.util.launch
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Handles error messages for command execution.
 */
object GlobalErrorHandler {
  /**
   * The default error message handler.
   */
  var errorHandler: (CommandContext<CommandSourceStack>, ComponentBuilder.() -> Unit) -> Unit = { context, consumer ->
    context.source.sender.sendMessage(
      component {
        text("✘ ") { red() }
        create(consumer)
      }
    )
  }

  /**
   * Sets a global error message handler.
   *
   * @param handler the handler to use
   */
  fun updateErrorHandler(handler: (CommandContext<CommandSourceStack>, ComponentBuilder.() -> Unit) -> Unit) {
    errorHandler = handler
  }
}

/**
 * Creates and manages Minecraft commands using a DSL-style builder pattern.
 *
 * This class wraps a Brigadier [ArgumentBuilder] and provides a Kotlin-friendly DSL
 * for defining command structures, including literals, arguments, requirements, and execution logic.
 *
 * @param T the type of Brigadier [ArgumentBuilder] being wrapped
 * @property plugin the plugin instance for launching coroutines
 * @property builder the underlying Brigadier [ArgumentBuilder]
 */
@Suppress("UnstableApiUsage")
@CommandDsl
class CommandCreator<T : ArgumentBuilder<CommandSourceStack, T>>(
  val plugin: Plugin?,
  var builder: T
) {
  /**
   * Adds a literal argument to the command.
   *
   * @param literal the literal string to match in the command
   * @return this [CommandCreator] instance for chaining
   */
  infix fun literal(
    literal: String
  ) = literal(literal) {}

  /**
   * Adds a literal argument to the command.
   *
   * @param literal the literal string to match in the command
   * @param creator the configuration block for the subcommand identified by the [literal]
   * @return this [CommandCreator] instance for chaining
   */
  fun literal(
    literal: String,
    creator: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit = {}
  ) = apply {
    val command = LiteralArgumentBuilder.literal<CommandSourceStack>(literal)
    val c = CommandCreator(plugin, command)
    c.creator()
    builder.then(c.builder)
  }


  /**
   * DSL operator to add a literal subcommand to the command.
   *
   * Example:
   * ```kotlin
   * "subcommand" {
   *   executes { ... }
   * }
   * ```
   *
   * @param creator the configuration block for the subcommand identified by the literal string
   */
  inline infix operator fun String.invoke(creator: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit) =
    apply {
      val command = LiteralArgumentBuilder.literal<CommandSourceStack>(this)
      val c = CommandCreator(plugin, command)
      c.creator()
      builder.then(c.builder)
    }

  /**
   * Adds multiple literal aliases to the command that all execute the same block.
   *
   * @param aliases the literal strings to match as aliases
   * @param creator the configuration block for the subcommand identified by the [aliases]
   * @return this [CommandCreator] instance for chaining
   */
  fun aliases(
    vararg aliases: String,
    creator: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit = {}
  ) = apply {
    aliases.forEach {
      val command = LiteralArgumentBuilder.literal<CommandSourceStack>(it)
      val c = CommandCreator(plugin, command)
      c.creator()
      builder.then(c.builder)
    }
  }

  /**
   * Adds a required argument to the command.
   *
   * @param V the type of the argument value
   * @param name the name of the argument
   * @param type the Brigadier [ArgumentType] for this argument
   * @param creator the configuration block for the command structure following this argument
   * @return this [CommandCreator] instance for chaining
   */
  fun <V> argument(
    name: String,
    type: ArgumentType<V>,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, V>>.() -> Unit = {}
  ) = apply {
    val arg = RequiredArgumentBuilder.argument<CommandSourceStack, V>(name, type)
    val c = CommandCreator(plugin, arg)
    c.creator()
    builder.then(c.builder)
  }

  /**
   * Adds a string argument to the command.
   */
  fun string(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.string(), creator)

  /**
   * Adds a greedy string argument to the command.
   */
  fun greedyString(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.greedyString(), creator)

  /**
   * Adds a word argument to the command.
   */
  fun word(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.word(), creator)

  /**
   * Adds an integer argument to the command.
   */
  fun integer(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Int>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.integer(min, max), creator)

  /**
   * Adds a boolean argument to the command.
   */
  fun boolean(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Boolean>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.boolean(), creator)

  /**
   * Adds a double argument to the command.
   */
  fun double(
    name: String,
    min: Double = Double.MIN_VALUE,
    max: Double = Double.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Double>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.double(min, max), creator)

  /**
   * Adds a float argument to the command.
   */
  fun float(
    name: String,
    min: Float = Float.MIN_VALUE,
    max: Float = Float.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Float>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.float(min, max), creator)

  /**
   * Adds a long argument to the command.
   */
  fun long(
    name: String,
    min: Long = Long.MIN_VALUE,
    max: Long = Long.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Long>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.long(min, max), creator)

  /**
   * Adds a player argument to the command.
   */
  fun player(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.player()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a players argument to the command.
   */
  fun players(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.players()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds an entity argument to the command.
   */
  fun entity(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.entity()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds an entities argument to the command.
   */
  fun entities(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.entities()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a world argument to the command.
   */
  fun world(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.world()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a block position argument to the command.
   */
  fun blockPos(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.blockPosition()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a fine position argument (with decimals) to the command.
   * Equivalent to [finePos] with `centerIntegers = false`.
   */
  fun finePos(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.finePosition(false)) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a rotation argument (yaw and pitch) to the command.
   */
  fun rotation(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.rotation()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  fun gameMode(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.gameMode(), creator)

  fun uuid(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.uuid(), creator)

  fun namespacedKey(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.namespacedKey(), creator)

  fun itemStack(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.itemStack(), creator)

  fun component(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.component(), creator)

  fun team(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.team(), creator)

  fun objective(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.objective(), creator)

  fun slot(name: String, creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}) =
    argument(name, DevCoreArgumentTypes.slot(), creator)

  fun columnBlockPos(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.columnBlockPosition(), creator)

  /**
   * Adds a column fine position argument (2D decimal coordinates) to the command.
   */
  fun columnFinePos(
    name: String,
    centerIntegers: Boolean = false,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.columnFinePosition(centerIntegers), creator)

  /**
   * Adds a fine position argument with centerIntegers option to the command.
   */
  fun finePos(
    name: String,
    centerIntegers: Boolean,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.finePosition(centerIntegers), creator)

  /**
   * Adds an angle argument to the command.
   */
  fun angle(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.angle(), creator)

  /**
   * Adds an axes argument (set of X/Y/Z axes) to the command.
   */
  fun axes(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.axes(), creator)

  /**
   * Adds a block state argument to the command.
   */
  fun blockState(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.blockState(), creator)

  /**
   * Adds a block-in-world predicate argument to the command.
   */
  fun blockInWorldPredicate(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.blockInWorldPredicate(), creator)

  /**
   * Adds an item stack predicate argument to the command.
   */
  fun itemStackPredicate(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.itemStackPredicate(), creator)

  /**
   * Adds a named color argument to the command.
   */
  fun namedColor(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.namedColor(), creator)

  /**
   * Adds a hex color argument to the command.
   */
  fun hexColor(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.hexColor(), creator)

  /**
   * Adds a style argument to the command.
   */
  fun style(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.style(), creator)

  /**
   * Adds a signed message argument to the command.
   */
  fun signedMessage(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.signedMessage(), creator)

  /**
   * Adds a scoreboard display slot argument to the command.
   */
  fun scoreboardDisplaySlot(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.scoreboardDisplaySlot(), creator)

  /**
   * Adds a key argument to the command.
   */
  fun key(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.key(), creator)

  /**
   * Adds an integer range argument to the command.
   */
  fun integerRange(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.integerRange(), creator)

  /**
   * Adds a double range argument to the command.
   */
  fun doubleRange(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.doubleRange(), creator)

  /**
   * Adds a height map argument to the command.
   */
  fun heightMap(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.heightMap(), creator)

  /**
   * Adds an objective criteria argument to the command.
   */
  fun objectiveCriteria(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.objectiveCriteria(), creator)

  /**
   * Adds an entity anchor argument to the command.
   */
  fun entityAnchor(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.entityAnchor(), creator)

  /**
   * Adds a time argument to the command.
   *
   * @param minTicks the minimum allowed time in ticks
   */
  fun time(
    name: String,
    minTicks: Int = 0,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.time(minTicks), creator)

  /**
   * Adds a template mirror argument to the command.
   */
  fun templateMirror(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.templateMirror(), creator)

  /**
   * Adds a template rotation argument to the command.
   */
  fun templateRotation(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.templateRotation(), creator)

  /**
   * Adds a resource key argument to the command.
   *
   * @param T the type of registry entry
   * @param registryKey the registry to use for validation
   */
  fun <T> resourceKey(
    name: String,
    registryKey: io.papermc.paper.registry.RegistryKey<T>,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.resourceKey(registryKey), creator)

  /**
   * Adds a resource argument to the command.
   *
   * @param T the type of registry entry
   * @param registryKey the registry to use for lookup
   */
  fun <T> resource(
    name: String,
    registryKey: io.papermc.paper.registry.RegistryKey<T>,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.resource(registryKey), creator)

  /**
   * Adds a player profiles argument to the command.
   */
  fun playerProfiles(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.playerProfiles(), creator)

  /**
   * Adds a slots argument (multiple inventory slots) to the command.
   */
  fun slots(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, DevCoreArgumentTypes.slots(), creator)

  /**
   * Sets a requirement predicate for this command.
   *
   * The command or subcommand will only be available and executable if the predicate returns true
   * for the given [CommandSourceStack].
   *
   * @param predicate a function that takes a [CommandSourceStack] and returns a [Boolean]
   * @return this [CommandCreator] instance for chaining
   */
  infix fun requires(predicate: (CommandSourceStack) -> Boolean) =
    apply {
      builder = builder.requires(predicate)
    }

  /**
   * Sets a requirement that the command sender must be an operator.
   *
   * The command or subcommand will only be available and executable if the sender has operator status.
   *
   * @return this [CommandCreator] instance for chaining
   */
  fun requireOp() =
    apply {
      builder = builder.requires { it.sender.isOp }
    }

  /**
   * Sets a requirement that the command sender must be an operator and satisfy an additional condition.
   *
   * The command or subcommand will only be available and executable if the sender has operator status
   * and the provided predicate returns true.
   *
   * @param predicate a function that takes a [CommandSourceStack] and returns a [Boolean]
   * @return this [CommandCreator] instance for chaining
   */
  fun requireOpAnd(predicate: (CommandSourceStack) -> Boolean) =
    apply {
      builder = builder.requires { it.sender.isOp && predicate(it) }
    }

  fun requirePermissionOrOp(permission: String) =
    requires { it.sender.isOp || it.sender.hasPermission(permission) }

  /**
   * Sets a permission requirement for this command.
   *
   * @param permission the permission string required to execute this command
   * @return this [CommandCreator] instance for chaining
   */
  infix fun permission(permission: String) = requires { it.sender.hasPermission(permission) }

  /**
   * Sets a permission requirement combined with an additional condition for this command.
   *
   * The command or subcommand will only be available and executable if the sender has the specified
   * permission and the provided predicate returns true.
   *
   * @param permission the permission string required to execute this command
   * @param predicate a function that takes a [CommandSourceStack] and returns a [Boolean]
   * @return this [CommandCreator] instance for chaining
   */
  fun permissionAnd(permission: String, predicate: (CommandSourceStack) -> Boolean) =
    requires { it.sender.hasPermission(permission) && predicate(it) }

  /**
   * Sets the execution handler for this command.
   *
   * This logic is executed when the command or subcommand is invoked.
   *
   * @param block the code to execute, taking a [CommandContext] and returning an integer result (usually 1 for success)
   * @return this [CommandCreator] instance for chaining
   */
  infix fun executes(block: (CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes(block)
    }

  infix fun executesSuspend(block: suspend (CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        plugin?.launch {
          block(context)
        }
        Command.SINGLE_SUCCESS
      }
    }

  /**
   * Sets the execution handler for this command, limited to players only.
   *
   * @param block the code to execute, taking the player and [CommandContext]
   * @return this [CommandCreator] instance for chaining
   */
  fun executesPlayer(block: (Player, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val player = context.source.sender as? Player
        if (player == null) {
          context.sendError { text("このコマンドはプレイヤーのみ実行可能です。") }
          0
        } else {
          block(player, context)
        }
      }
    }

  infix fun executesPlayerSuspend(block: suspend (Player, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val player = context.source.sender as? Player
        if (player == null) {
          context.sendError { text("このコマンドはプレイヤーのみ実行可能です。") }
          0
        } else {
          plugin?.launch {
            block(player, context)
          }
          Command.SINGLE_SUCCESS
        }
      }
    }

  /**
   * Sets the execution handler for this command, limited to console only.
   *
   * @param block the code to execute, taking the console sender and [CommandContext]
   * @return this [CommandCreator] instance for chaining
   */
  fun executesConsole(block: (ConsoleCommandSender, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val console = context.source.sender as? ConsoleCommandSender
        if (console == null) {
          context.sendError { text("このコマンドはコンソールのみ実行可能です。") }
          0
        } else {
          block(console, context)
        }
      }
    }

  infix fun executesConsoleSuspend(block: suspend (ConsoleCommandSender, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val console = context.source.sender as? ConsoleCommandSender
        if (console == null) {
          context.sendError { text("このコマンドはコンソールのみ実行可能です。") }
          0
        } else {
          var result = 0
          plugin?.launch {
            result = block(console, context)
          }
          result
        }
      }
    }

  /**
   * Sends a message to the command sender using Adventure component DSL.
   *
   * @param consumer the configuration block for the component
   */
  fun CommandContext<CommandSourceStack>.sendMessage(consumer: ComponentBuilder.() -> Unit) {
    source.sender.sendMessage(component(consumer = consumer))
  }

  /**
   * Sends a success message to the command sender.
   */
  fun CommandContext<CommandSourceStack>.sendSuccess(consumer: ComponentBuilder.() -> Unit) {
    sendMessage {
      text("✔ ") { green() }
      create(consumer)
    }
  }

  /**
   * Sends an error message to the command sender.
   */
  fun CommandContext<CommandSourceStack>.sendError(consumer: ComponentBuilder.() -> Unit) {
    GlobalErrorHandler.errorHandler(this, consumer)
  }

  /**
   * Guards the command execution with a condition.
   * If the condition is false, sends an error message and returns 0.
   *
   * @param condition the condition to check
   * @param errorMessage the error message to send if condition is false
   * @param block the code to execute if condition is true
   * @return 1 if successful, 0 otherwise
   */
  fun CommandContext<CommandSourceStack>.guard(
    condition: Boolean,
    errorMessage: ComponentBuilder.() -> Unit,
    block: () -> Int
  ): Int {
    return if (condition) {
      block()
    } else {
      sendError(errorMessage)
      0
    }
  }

  /**
   * Sends a message to the command sender.
   *
   * @param component the component to send
   */
  fun CommandContext<CommandSourceStack>.sendMessage(component: Component) {
    source.sender.sendMessage(component)
  }

  /**
   * Adds static suggestions for this command argument.
   *
   * @param suggestions a list of strings to be suggested to the user
   * @return this [CommandCreator] instance for chaining
   */
  fun suggestion(suggestions: List<String>) =
    suggestion { _, builder ->
      val remaining = builder.remaining.lowercase()
      suggestions.forEach {
        if (it.lowercase().startsWith(remaining)) {
          builder.suggest(it)
        }
      }
      builder.buildFuture()
    }

  /**
   * Adds static suggestions for this command argument from an [Enum].
   *
   * @param E the enum type whose values will be suggested (lowercase)
   * @return this [CommandCreator] instance for chaining
   */
  inline fun <reified E : Enum<E>> suggestion() =
    suggestion(enumValues<E>().map { it.name.lowercase() })

  /**
   * Adds asynchronous suggestions for this command argument.
   *
   * @param provider a function that returns a list of strings to be suggested
   * @return this [CommandCreator] instance for chaining
   */
  fun suggestionAsync(provider: (CommandContext<CommandSourceStack>) -> List<String>) =
    suggestion { context, builder ->
      val remaining = builder.remaining.lowercase()
      provider(context).forEach {
        if (it.lowercase().startsWith(remaining)) {
          builder.suggest(it)
        }
      }
      builder.buildFuture()
    }

  /**
   * Adds dynamic suggestions for this command argument.
   *
   * @param provider the Brigadier [SuggestionProvider] that generates suggestions
   * @return this [CommandCreator] instance for chaining
   */
  fun suggestion(provider: SuggestionProvider<CommandSourceStack>) =
    apply {
      val currentBuilder = builder
      if (currentBuilder is RequiredArgumentBuilder<*, *>) {
        @Suppress("UNCHECKED_CAST")
        builder = (currentBuilder as RequiredArgumentBuilder<CommandSourceStack, Any>).suggests(provider) as T
      }
    }

  /**
   * Registers this command with the given plugin.
   *
   * This method must be called on the top-level [CommandCreator] (which should wrap a [LiteralArgumentBuilder])
   * to actually register the command with the Minecraft server.
   *
   * @param description the command description, or null if not provided
   * @param aliases the command aliases, or empty list if not provided
   */
  fun register(description: String?, aliases: Collection<String>) {
    val currentBuilder = builder
    if (currentBuilder is LiteralArgumentBuilder<*>) {
      @Suppress("UNCHECKED_CAST")
      plugin?.lifecycleManager?.registerEventHandler(LifecycleEvents.COMMANDS) {
        it.registrar().register(
          (currentBuilder as LiteralArgumentBuilder<CommandSourceStack>).build(),
          if (description.isNullOrBlank() || description.isEmpty()) null else description,
          aliases
        )
      }
    } else {
      plugin?.logger?.warning("コマンドのトップレベルは LiteralArgumentBuilder である必要があります。")
    }
  }
}
