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
import neotypes.{AsyncDriver, GraphDatabase, TransactionConfig}
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.integration.tests.WithItDb.ItDb

import java.util.UUID
import scala.concurrent.duration.DurationInt
import neotypes.cats.effect.implicits.*
import neotypes.mappers.ResultMapper
import neotypes.model.types.Node
import neotypes.query.DeferredQueryBuilder
import neotypes.syntax.all.*
import org.typelevel.log4cats.Logger

trait WithItDb:
  self: IntegrationSpecWithResource[?] =>

  private def createDb(config: Neo4jConnectionConf, driver: AsyncDriver[IO], dbName: String): IO[ItDb] =
    for
      _ <- c"CREATE DATABASE $dbName".execute.void(driver)
      _ <- c"START DATABASE $dbName".execute.void(driver)
      _ <- IO.sleep(2.second) // Wait for the database to start
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

  extension (builder: DeferredQueryBuilder)
    def queryList[T](mapper: ResultMapper[T])(implicit db: WithItDb.ItDb): IO[List[T]] =
      builder.query(mapper).list(db.driver, TransactionConfig.default.withDatabase(db.dbName)).logValue

    def querySingle[T](mapper: ResultMapper[T])(implicit db: WithItDb.ItDb): IO[T] =
      def query = builder.query(mapper)
      queryList(mapper).logValue.flatMap:
        case head :: Nil => IO.pure(head)
        case Nil         => IO.raiseError(new AssertionError(s"No result found for query: $query"))
        case res         => IO.raiseError(new AssertionError(s"More then one result found: $res, for query: $query"))

    def listNode(implicit db: WithItDb.ItDb): IO[List[Node]] = queryList(ResultMapper.node)
    def singleNode(implicit db: WithItDb.ItDb): IO[Node] = querySingle(ResultMapper.node)

object WithItDb:
  final case class ItDb(config: Neo4jConnectionConf, driver: AsyncDriver[IO], dbName: String)
  final case class ResourceWithDb[R](resource: R, itDb: ItDb)
