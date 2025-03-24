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

import cats.effect.{IO, Resource, Sync}
import com.typesafe.config.ConfigFactory
import cats.effect.cps.*
import neotypes.model.types.Node
import org.typelevel.log4cats.Logger
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.core.database.Neo4jDatabase
import planning.engine.integration.tests.IntegrationSpecWithResource

import scala.concurrent.duration.{Duration, DurationInt}



class Neo4jDatabaseIntegrationSpec extends IntegrationSpecWithResource[Neo4jDatabase[IO]]:
  val TEST_DATABASE_NAME = "pacman-5x3-two-ways"
  override val ResourceTimeout: Duration = 10.seconds

  override val resource: Resource[IO, Neo4jDatabase[IO]] = logResource(
    for
      config <- Resource.eval(Neo4jConnectionConf
        .formConfig[IO](ConfigFactory.load("test_db.conf").getConfig("test_db.neo4j")))

      neo4jDatabase <- Neo4jDatabase[IO](config, TEST_DATABASE_NAME)
    yield neo4jDatabase)

  "Neo4jDatabase.readRootNode" should:
    "read root node" in: neo4jDatabase =>
      async[IO]:
        val result = neo4jDatabase.readRootNode.await

        Logger[IO].info("Root node: " + result).await

        result mustBe a [Node]
