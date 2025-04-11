/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 2025-03-15 |||||||||||*/

package planning.engine.integration.tests.database

import cats.effect.{IO, Resource}
import cats.effect.cps.*
import neotypes.model.types.Node
import org.typelevel.log4cats.Logger
import planning.engine.core.database.Neo4jDatabase
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}

import scala.concurrent.duration.{Duration, DurationInt}

class Neo4jDatabaseIntegrationSpec extends IntegrationSpecWithResource[Neo4jDatabase[IO]] with WithItDb:
  override val ResourceTimeout: Duration = 60.seconds

  override val resource: Resource[IO, Neo4jDatabase[IO]] =
    for
      itDb <- makeDb
      neo4jdb <- Neo4jDatabase[IO](itDb.config, itDb.dbName)
    yield neo4jdb

  "Neo4jDatabase.readRootNode" should:
    "read root node" in: neo4jDatabase =>
      async[IO]:
        val result = neo4jDatabase.readRootNode.await
        Logger[IO].info("Root node: " + result).await
        result mustBe a[Node]
