package com.peco2282.devcore.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.peco2282.devcore.adventure.builder.Componenter
import com.peco2282.devcore.adventure.component
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

/**
 * Handles error messages for command execution.
 */
object GlobalErrorHandler {
  /**
   * The default error message handler.
   */
  var errorHandler: (CommandContext<CommandSourceStack>, Componenter.() -> Unit) -> Unit = { context, consumer ->
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
  fun setErrorHandler(handler: (CommandContext<CommandSourceStack>, Componenter.() -> Unit) -> Unit) {
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
 * @property builder the underlying Brigadier [ArgumentBuilder]
 */
@Suppress("UnstableApiUsage")
@CommandDsl
class CommandCreator<T : ArgumentBuilder<CommandSourceStack, T>>(
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
    val c = CommandCreator(command)
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
      val c = CommandCreator(command)
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
    val c = CommandCreator(arg)
    c.creator()
    builder.then(c.builder)
  }

  /**
   * Adds a sub-command structure defined by a literal.
   *
   * @param name the literal for the sub-command
   * @param creator the configuration block for the sub-command
   * @return this [CommandCreator] instance for chaining
   */
  fun sub(
    name: String,
    creator: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit
  ) = literal(name, creator)

  /**
   * Adds a string argument to the command.
   */
  fun string(
    name: String,
    type: StringArgumentType = StringArgumentType.string(),
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = argument(name, type, creator)

  /**
   * Adds a greedy string argument to the command.
   */
  fun greedyString(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = string(name, StringArgumentType.greedyString(), creator)

  /**
   * Adds a word argument to the command.
   */
  fun word(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, String>>.() -> Unit = {}
  ) = string(name, StringArgumentType.word(), creator)

  /**
   * Adds an integer argument to the command.
   */
  fun integer(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Int>>.() -> Unit = {}
  ) = argument(name, IntegerArgumentType.integer(min, max), creator)

  /**
   * Adds a boolean argument to the command.
   */
  fun boolean(
    name: String,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Boolean>>.() -> Unit = {}
  ) = argument(name, BoolArgumentType.bool(), creator)

  /**
   * Adds a double argument to the command.
   */
  fun double(
    name: String,
    min: Double = -Double.MAX_VALUE,
    max: Double = Double.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Double>>.() -> Unit = {}
  ) = argument(name, DoubleArgumentType.doubleArg(min, max), creator)

  /**
   * Adds a float argument to the command.
   */
  fun float(
    name: String,
    min: Float = -Float.MAX_VALUE,
    max: Float = Float.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Float>>.() -> Unit = {}
  ) = argument(name, FloatArgumentType.floatArg(min, max), creator)

  /**
   * Adds a long argument to the command.
   */
  fun long(
    name: String,
    min: Long = Long.MIN_VALUE,
    max: Long = Long.MAX_VALUE,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, Long>>.() -> Unit = {}
  ) = argument(name, LongArgumentType.longArg(min, max), creator)

  /**
   * Adds a player argument to the command.
   */
  fun player(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.player()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a players argument to the command.
   */
  fun players(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.players()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds an entity argument to the command.
   */
  fun entity(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.entity()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds an entities argument to the command.
   */
  fun entities(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.entities()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a world argument to the command.
   */
  fun world(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.world()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a block position argument to the command.
   */
  fun blockPos(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.blockPosition()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a fine position argument (with decimals) to the command.
   */
  fun finePos(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.finePosition()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a rotation argument (yaw and pitch) to the command.
   */
  fun rotation(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.rotation()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a single player argument to the command.
   */
  fun singlePlayer(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.player()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a multiple players argument to the command.
   */
  fun multiplePlayers(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.players()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a single entity argument to the command.
   */
  fun singleEntity(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.entity()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

  /**
   * Adds a multiple entities argument to the command.
   */
  fun multipleEntities(
    name: String,
    creator: CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>.() -> Unit = {}
  ) = argument(name, ArgumentTypes.entities()) {
    @Suppress("UNCHECKED_CAST")
    (this as CommandCreator<out ArgumentBuilder<CommandSourceStack, *>>).creator()
  }

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

  /**
   * Sets the execution handler for this command, limited to players only.
   *
   * @param block the code to execute, taking the player and [CommandContext]
   * @return this [CommandCreator] instance for chaining
   */
  fun executesPlayer(block: (org.bukkit.entity.Player, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val player = context.source.sender as? org.bukkit.entity.Player
        if (player == null) {
          context.sendError { text("このコマンドはプレイヤーのみ実行可能です。") }
          0
        } else {
          block(player, context)
        }
      }
    }

  /**
   * Sets the execution handler for this command, limited to console only.
   *
   * @param block the code to execute, taking the console sender and [CommandContext]
   * @return this [CommandCreator] instance for chaining
   */
  fun executesConsole(block: (org.bukkit.command.ConsoleCommandSender, CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes { context ->
        val console = context.source.sender as? org.bukkit.command.ConsoleCommandSender
        if (console == null) {
          context.sendError { text("このコマンドはコンソールのみ実行可能です。") }
          0
        } else {
          block(console, context)
        }
      }
    }

  /**
   * Adds a subcommand to this command.
   *
   * This is an alias for [literal] to improve readability in complex command trees.
   *
   * @param name the name of the subcommand
   * @param creator the configuration block for the subcommand
   * @return this [CommandCreator] instance for chaining
   */
  fun subcommand(
    name: String,
    creator: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit = {}
  ) = literal(name, creator)

  /**
   * Sends a message to the command sender using Adventure component DSL.
   *
   * @param consumer the configuration block for the component
   */
  fun CommandContext<CommandSourceStack>.sendMessage(consumer: Componenter.() -> Unit) {
    source.sender.sendMessage(component(consumer = consumer))
  }

  /**
   * Sends a success message to the command sender.
   */
  fun CommandContext<CommandSourceStack>.sendSuccess(consumer: Componenter.() -> Unit) {
    sendMessage {
      text("✔ ") { green() }
      create(consumer)
    }
  }

  /**
   * Sends an error message to the command sender.
   */
  fun CommandContext<CommandSourceStack>.sendError(consumer: Componenter.() -> Unit) {
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
    errorMessage: Componenter.() -> Unit,
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
   * @param enumClass the class of the enum to suggest values from
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
   * @param plugin the [Plugin] instance to register the command under
   */
  fun register(plugin: Plugin) {
    val currentBuilder = builder
    if (currentBuilder is LiteralArgumentBuilder<*>) {
      @Suppress("UNCHECKED_CAST")
      plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
        it.registrar().register((currentBuilder as LiteralArgumentBuilder<CommandSourceStack>).build(), null)
      }
    } else {
      plugin.logger.warning("コマンドのトップレベルは LiteralArgumentBuilder である必要があります。")
    }
  }
}
