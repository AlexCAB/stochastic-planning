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
import planning.engine.map.database.Neo4jConf
import planning.engine.integration.tests.WithItDb.ItDb

import java.util.UUID
import scala.concurrent.duration.DurationInt
import neotypes.cats.effect.implicits.*
import neotypes.mappers.ResultMapper
import neotypes.model.types.{Node, Value}
import neotypes.query.DeferredQueryBuilder
import neotypes.syntax.all.*
import cats.syntax.all.*
import planning.engine.common.values.node.HnId
import planning.engine.common.properties.*

trait WithItDb:
  self: IntegrationSpecWithResource[?] =>

  val removeDbAfterTest: Boolean = true

  private def createDb(config: Neo4jConf, driver: AsyncDriver[IO], dbName: String): IO[ItDb] =
    for
      _ <- c"CREATE DATABASE $dbName".execute.void(driver)
      _ <- c"START DATABASE $dbName".execute.void(driver)
      _ <- IO.sleep(2.second) // Wait for the database to start
      _ <- logInfo("createDb", s"[WithItDb] Database $dbName created and started.")
    yield ItDb(config, driver, dbName)

  private def dropDb(driver: AsyncDriver[IO], dbName: String): IO[Unit] =
    if removeDbAfterTest then
      for
        _ <- c"STOP DATABASE $dbName".execute.void(driver)
        _ <- c"DROP DATABASE $dbName DESTROY DATA".execute.void(driver)
        _ <- logInfo("dropDb", s"[WithItDb] Database $dbName stopped and dropped.")
        _ <- IO.sleep(2.second) // Wait for the database to be dropped
      yield ()
    else
      logInfo("dropDb", s"[WithItDb] (!!!) Database $dbName will not be dropped as removeDbAfterTest is set to false.")

  def makeDb(): Resource[IO, ItDb] =
    for
      config <- Resource.eval(Neo4jConf
        .formConfig[IO](ConfigFactory.load("it_test_db.conf").getConfig("it_test_db.neo4j")))
      dbName = "integration-test-db-" + UUID.randomUUID().toString
      driver <- GraphDatabase.asyncDriver[IO](config.uri, config.authToken)
      itDb <- Resource.make(createDb(config, driver, dbName))(id => dropDb(id.driver, id.dbName))
    yield itDb

  extension (builder: DeferredQueryBuilder)
    def queryList[T](mapper: ResultMapper[T])(implicit db: WithItDb.ItDb): IO[List[T]] = builder
      .query(mapper).list(db.driver, TransactionConfig.default.withDatabase(db.dbName))
      .flatMap(_.traverseTap(n => logInfo("queryList", s"node = $n")))
      .logValue("queryList")

    def querySingle[T](mapper: ResultMapper[T])(implicit db: WithItDb.ItDb): IO[T] =
      def query = builder.query(mapper)
      queryList(mapper).flatMap:
        case head :: Nil => IO.pure(head)
        case Nil         => IO.raiseError(new AssertionError(s"No result found for query: $query"))
        case res         => IO.raiseError(new AssertionError(s"More then one result found: $res, for query: $query"))

    def listNode(implicit db: WithItDb.ItDb): IO[List[Node]] = queryList(ResultMapper.node)
    def singleNode(implicit db: WithItDb.ItDb): IO[Node] = querySingle(ResultMapper.node)

    def listHnIds(implicit db: WithItDb.ItDb): IO[List[HnId]] = queryList(ResultMapper.node)
      .flatMap(_.traverse(_.getValue[IO, Long](PROP.HN_ID).map(HnId.apply)))
      .flatTap(ids => logInfo("listHnIds", s"hnIds = $ids"))

    def count(implicit db: WithItDb.ItDb): IO[Long] =
      builder.querySingle(ResultMapper.long).logValue("countNodes", "count")

  extension (node: Node)
    def getProperty(key: String)(implicit db: WithItDb.ItDb): Value =
      node.properties.getOrElse(key, fail(s"Not found property $key in $node"))

    def getStringProperty(key: String)(implicit db: WithItDb.ItDb): String = node.getProperty(key) match
      case Value.Str(v) => v
      case _            => fail(s"Not found String property $key in $node")

    def getLongProperty(key: String)(implicit db: WithItDb.ItDb): Long = node.getProperty(key) match
      case Value.Integer(v) => v
      case _                => fail(s"Not found Long property $key in $node")

object WithItDb:
  final case class ItDb(config: Neo4jConf, driver: AsyncDriver[IO], dbName: String)
  final case class ResourceWithDb[R](resource: R, itDb: ItDb)
