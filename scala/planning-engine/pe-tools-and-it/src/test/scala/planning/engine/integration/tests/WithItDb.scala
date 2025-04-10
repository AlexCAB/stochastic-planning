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
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.config.ConfigFactory
import neotypes.{AsyncDriver, GraphDatabase}
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.integration.tests.WithItDb.ItDb
import java.util.UUID

import neotypes.cats.effect.implicits.*
import neotypes.syntax.all.*

trait WithItDb:
  self: AsyncIOSpec =>

  private def createDb(dbName: String, driver: AsyncDriver[IO]): IO[Unit] =
    for
      _ <- c"CREATE DATABASE $dbName".execute.void(driver)
      _ <- c"START DATABASE $dbName".execute.void(driver)
    yield ()

  private def dropDb(dbName: String, driver: AsyncDriver[IO]): IO[Unit] =
    for
      _ <- c"STOP DATABASE $dbName".execute.void(driver)
      _ <- c"DROP DATABASE $dbName".execute.void(driver)
    yield ()

  def makeDb: Resource[IO, ItDb] =
    for
      config <- Resource.eval(Neo4jConnectionConf
        .formConfig[IO](ConfigFactory.load("it_test_db.conf").getConfig("it_test_db.neo4j")))
      dbName = "integration-test-db-" + UUID.randomUUID().toString
      driver <- GraphDatabase.asyncDriver[IO](config.uri, config.authToken)
      _ <- Resource.make(createDb(dbName, driver))(_ => dropDb(dbName, driver))
    yield ItDb(driver, dbName)

object WithItDb:
  final case class ItDb(driver: AsyncDriver[IO], dbName: String)
  final case class ResourceWithDb[R](resource: R, itDb: ItDb)
