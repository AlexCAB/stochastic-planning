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
| created: 2025-04-11 |||||||||||*/

package planning.engine.integration.tests

import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import neotypes.{AsyncDriver, GraphDatabase}
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.integration.tests.WithItDb.ItDb

import java.util.UUID
import scala.concurrent.duration.DurationInt
import neotypes.cats.effect.implicits.*
import neotypes.syntax.all.*
import org.typelevel.log4cats.Logger

trait WithItDb:
  self: IntegrationSpecWithResource[?] =>

  private def createDb(config: Neo4jConnectionConf, driver: AsyncDriver[IO], dbName: String): IO[ItDb] =
    for
      _ <- c"CREATE DATABASE $dbName".execute.void(driver)
      _ <- c"START DATABASE $dbName".execute.void(driver)
      _ <- IO.sleep(2.second)  // Wait for the database to start
      _ <- Logger[IO].info(s"[WithItDb] Database $dbName created and started.")
    yield ItDb(config, driver, dbName)

  private def dropDb(driver: AsyncDriver[IO], dbName: String): IO[Unit] =
    for
      _ <- c"STOP DATABASE $dbName".execute.void(driver)
      _ <- c"DROP DATABASE $dbName DESTROY DATA".execute.void(driver)
      _ <- Logger[IO].info(s"[WithItDb] Database $dbName stopped and dropped.")
      _ <- IO.sleep(2.second) // Wait for the database to be dropped
    yield ()

  def makeDb: Resource[IO, ItDb] =
    for
      config <- Resource.eval(Neo4jConnectionConf
        .formConfig[IO](ConfigFactory.load("it_test_db.conf").getConfig("it_test_db.neo4j")))
      dbName = "integration-test-db-" + UUID.randomUUID().toString
      driver <- GraphDatabase.asyncDriver[IO](config.uri, config.authToken)
      itDb <- Resource.make(createDb(config, driver, dbName))(id => dropDb(id.driver, id.dbName))
    yield itDb

object WithItDb:
  final case class ItDb(config: Neo4jConnectionConf, driver: AsyncDriver[IO], dbName: String)
  final case class ResourceWithDb[R](resource: R, itDb: ItDb)
