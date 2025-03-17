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
import cats.effect.testing.specs2.CatsResource
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.SpecificationLike
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.core.database.Neo4jDatabase
import cats.effect.cps.*
import neotypes.model.types.Node
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger



class Neo4jDatabaseIntegrationSpec extends CatsResource[IO, Neo4jDatabase[IO]] with SpecificationLike:
  val TEST_DATABASE_NAME = "pacman-5x3-two-ways"

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  override val resource: Resource[IO, Neo4jDatabase[IO]] =
    for
      config <- Resource.eval(
        Neo4jConnectionConf.formConfig[IO](ConfigFactory.load("test_db.conf").getConfig("test_db.neo4j")))
      neo4jDatabase <- Neo4jDatabase[IO](config, TEST_DATABASE_NAME)
    yield neo4jDatabase

  "Neo4jDatabase.readRootNode" should:
    "read root node" in withResource: neo4jDatabase =>
      async[IO]:
        val result = neo4jDatabase.readRootNode.await

        Logger[IO].info("Root node: " + result).await

        result must beAnInstanceOf[Node]

