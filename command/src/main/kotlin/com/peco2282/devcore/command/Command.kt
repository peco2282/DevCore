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
   * @param argument the name of the argument
   * @param type the Brigadier [ArgumentType] for this argument
   * @param creator the configuration block for the command structure following this argument
   * @return this [CommandCreator] instance for chaining
   */
  fun <V> argument(
    argument: String,
    type: ArgumentType<V>,
    creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, V>>.() -> Unit = {}
  ) = apply {
    val arg = RequiredArgumentBuilder.argument<CommandSourceStack, V>(argument, type)
    val c = CommandCreator(arg)
    c.creator()
    builder.then(c.builder)
  }

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
   * Sets a permission requirement for this command.
   *
   * @param permission the permission string required to execute this command
   * @return this [CommandCreator] instance for chaining
   */
  infix fun permission(permission: String) = requires { it.sender.hasPermission(permission) }

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
    sendMessage {
      text("✘ ") { red() }
      create(consumer)
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
        it.registrar().register((currentBuilder as LiteralArgumentBuilder<CommandSourceStack>).build())
      }
    } else {
      plugin.logger.warning("コマンドのトップレベルは LiteralArgumentBuilder である必要があります。")
    }
  }
}
