package com.peco2282.devcore.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
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
   * Adds static suggestions for this command argument.
   *
   * @param suggestions a list of strings to be suggested to the user
   * @return this [CommandCreator] instance for chaining
   */
  fun suggestion(suggestions: List<String>) =
    suggestion { _, builder ->
      suggestions.forEach { builder.suggest(it) }
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
