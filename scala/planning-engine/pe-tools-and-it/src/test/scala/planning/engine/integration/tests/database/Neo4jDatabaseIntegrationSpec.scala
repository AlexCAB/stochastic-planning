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
import planning.engine.integration.tests.WithItDb.ResourceWithDb
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}

import scala.concurrent.duration.{Duration, DurationInt}

class Neo4jDatabaseIntegrationSpec extends IntegrationSpecWithResource[ResourceWithDb[Neo4jDatabase[IO]]] with WithItDb:
  override val ResourceTimeout: Duration = 10.seconds

  override val resource: Resource[IO, ResourceWithDb[Neo4jDatabase[IO]]] = logResource(
    makeDb.map(itDb => ResourceWithDb(new Neo4jDatabase[IO](itDb.driver, itDb.dbName), itDb))
  )

  "Neo4jDatabase.readRootNode" should:
    "read root node" in: neo4jDatabase =>
      async[IO]:
        val result = neo4jDatabase.resource.readRootNode.await

        Logger[IO].info("Root node: " + result).await

        result mustBe a[Node]
