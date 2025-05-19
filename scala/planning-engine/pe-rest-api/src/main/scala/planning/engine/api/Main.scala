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
| created: 2025-04-19 |||||||||||*/

package planning.engine.api

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toSemigroupKOps
import fs2.io.net.Network
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.server.middleware.{ErrorHandling, Logger}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import planning.engine.api.config.{MainConf, ServerConf}
import planning.engine.api.route.maintenance.MaintenanceRoute
import planning.engine.api.route.map.MapRoute
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.api.service.map.MapService
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.knowledge.graph.KnowledgeGraphBuilder

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationInt

object Main extends IOApp:
  type Middleware = HttpRoutes[IO] => HttpRoutes[IO]

  private given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val shutdownTimeout: FiniteDuration = 1.seconds
  private val loggerService: Middleware = Logger.httpRoutes[IO](logHeaders = true, logBody = true)
  private val errorHandlingService = ErrorHandling.httpRoutes[IO]

  private def buildHttpApp(apiPrefix: String, routes: HttpRoutes[IO]): HttpApp[IO] =
    loggerService(errorHandlingService(Router(apiPrefix -> routes))).orNotFound

  private def buildServer(config: ServerConf, routes: HttpRoutes[IO]): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(config.host)
    .withPort(config.port)
    .withHttpApp(buildHttpApp(config.apiPrefix, routes))
    .withShutdownTimeout(shutdownTimeout)
    .build

  private def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      mainConf <- MainConf.default[IO]
      database <- Neo4jDatabase[IO](mainConf.db.connection, mainConf.db.name)
      builder <- KnowledgeGraphBuilder[IO](database)

      maintenanceService <- MaintenanceService[IO]()
      maintenanceRoute <- MaintenanceRoute(maintenanceService)

      mapService <- MapService[IO](mainConf.knowledgeGraph, builder)
      mapRoute <- MapRoute(mapService)

      rootRoute = maintenanceRoute.endpoints <+> mapRoute.endpoints

      _ <- buildServer(mainConf.server, rootRoute)
    yield maintenanceService

  def run(args: List[String]): IO[ExitCode] = buildApp()
    .use(_.awaitShutdown).as(ExitCode.Success)
