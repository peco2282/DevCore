package com.peco2282.devcore.database.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

/**
 * Interface for MongoDB database access.
 */
interface MongoProvider : AutoCloseable {
  val client: MongoClient
  val database: MongoDatabase

  /**
   * Returns a [MongoCollection] for the given name.
   */
  fun getCollection(name: String): MongoCollection<Document> = database.getCollection(name)

  /**
   * Returns a [MongoCollection] for the given name and class.
   */
  fun <T : Any> getCollection(name: String, clazz: Class<T>): MongoCollection<T> =
    database.getCollection(name, clazz)
}

/**
 * Creates a [MongoProvider].
 *
 * @param action The configuration action for the builder.
 * @return A configured [MongoProvider].
 */
fun createMongo(action: MongoBuilder.() -> Unit): MongoProvider = MongoBuilder().apply(action).build()

/**
 * Builder class for [MongoProvider].
 */
class MongoBuilder {
  var connectionString: String = "mongodb://localhost:27017"
  var databaseName: String = "minecraft"

  fun build(): MongoProvider {
    val client = MongoClients.create(connectionString)
    val database = client.getDatabase(databaseName)
    return MongoProviderImpl(client, database)
  }
}

internal class MongoProviderImpl(
  override val client: MongoClient,
  override val database: MongoDatabase
) : MongoProvider {
  override fun close() {
    client.close()
  }
}
