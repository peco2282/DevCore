package com.peco2282.devcore.command.v1_21_1

import com.mojang.brigadier.arguments.ArgumentType
import io.papermc.paper.command.brigadier.argument.VanillaArgumentProviderImpl
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Proxy

internal object NativeWrapperFactory {

  private val ctor: MethodHandle
  private val converterInterface: Class<*>

  init {
    val wrapperClass = VanillaArgumentProviderImpl.NativeWrapperArgumentType::class.java

    converterInterface =
      Class.forName(
        "io.papermc.paper.command.brigadier.argument.VanillaArgumentProviderImpl\$ResultConverter"
      )

    val lookup = MethodHandles.privateLookupIn(
      wrapperClass,
      MethodHandles.lookup()
    )

    ctor = lookup.findConstructor(
      wrapperClass,
      MethodType.methodType(
        Void.TYPE,
        ArgumentType::class.java,
        converterInterface
      )
    )
  }

  @Suppress("UNCHECKED_CAST")
  fun <M : Any, P : Any> wrap(
    native: ArgumentType<M>,
    converter: (M) -> P
  ): ArgumentType<P> {

    val proxy = Proxy.newProxyInstance(
      converterInterface.classLoader,
      arrayOf(converterInterface)
    ) { _, method, args ->

      when (method.name) {
        "convert" -> converter(args[0] as M)
        else -> throw UnsupportedOperationException(method.name)
      }
    }

    return ctor.invoke(native, proxy) as ArgumentType<P>
  }
}