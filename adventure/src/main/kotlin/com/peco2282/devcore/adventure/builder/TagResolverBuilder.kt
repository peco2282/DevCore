package com.peco2282.devcore.adventure.builder

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

interface TagResolverBuilder {
  infix fun append(resolver: TagResolver): TagResolverBuilder
  infix fun append(resolver: Array<out TagResolver>): TagResolverBuilder
  fun empty(): TagResolverBuilder = append(TagResolver.empty())
  fun standard(): TagResolverBuilder = append(TagResolver.standard())
  fun resolvers(vararg resolvers: TagResolver): TagResolverBuilder = append(resolvers)

  operator fun TagResolver.unaryPlus(): TagResolverBuilder = append(this)
  operator fun Array<TagResolver>.unaryPlus(): TagResolverBuilder = append(this)
  infix fun and(resolver: TagResolver): TagResolverBuilder = append(resolver)
  infix fun and(resolvers: Array<TagResolver>): TagResolverBuilder = append(resolvers)

  fun build(): Array<TagResolver>
}