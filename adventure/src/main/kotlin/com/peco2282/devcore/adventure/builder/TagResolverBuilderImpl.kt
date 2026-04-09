package com.peco2282.devcore.adventure.builder

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class TagResolverBuilderImpl : TagResolverBuilder {
  private val resolvers = mutableListOf<TagResolver>()

  override fun append(resolver: TagResolver): TagResolverBuilder = apply {
    resolvers.add(resolver)
  }

  override fun append(resolver: Array<out TagResolver>): TagResolverBuilder = apply {
    resolvers.addAll(resolver)
  }

  override fun build(): Array<TagResolver> = resolvers.toTypedArray()
}
