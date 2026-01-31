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
 * @property parent The parent command creator, if this is a subcommand
 */
@Suppress("UnstableApiUsage")
@CommandDsl
class CommandCreator<T : ArgumentBuilder<CommandSourceStack, T>>(
  var builder: T
) {
  /**
   * Adds a literal argument to the command.
   *
   * @param literal The literal string to match
   * @param creator The configuration block for this command
   * @return This command creator instance
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
   * @param argument The name of the argument
   * @param type The argument type
   * @param creator The configuration block for this command
   * @return This command creator instance
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
   * @param predicate The predicate that must be satisfied to execute this command
   * @return This command creator instance
   */
  infix fun requires(predicate: (CommandSourceStack) -> Boolean) =
    apply {
      builder = builder.requires(predicate)
    }

  /**
   * Sets the execution handler for this command.
   *
   * @param block The code to execute when this command is run
   * @return This command creator instance
   */
  infix fun executes(block: (CommandContext<CommandSourceStack>) -> Int) =
    apply {
      builder = builder.executes(block)
    }

  /**
   * Adds static suggestions for this command argument.
   *
   * @param suggestions List of suggestion strings
   * @return This command creator instance
   */
  fun suggestion(suggestions: List<String>) =
    suggestion { _, builder ->
      suggestions.forEach { builder.suggest(it) }
      builder.buildFuture()
    }

  /**
   * Adds dynamic suggestions for this command argument.
   *
   * @param provider The suggestion provider
   * @return This command creator instance
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
   * @param plugin The plugin to register the command with
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
