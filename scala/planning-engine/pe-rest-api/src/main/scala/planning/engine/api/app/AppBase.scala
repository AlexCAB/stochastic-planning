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
| created: 2025-12-23 |||||||||||*/

package planning.engine.api.app

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import planning.engine.api.config.parts.ServerConf
import planning.engine.api.route.maintenance.MaintenanceRoute
import planning.engine.api.service.maintenance.MaintenanceService

import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class AppBase extends IOApp:
  import AppBase.Maintenance

  private type Middleware = HttpRoutes[IO] => HttpRoutes[IO]

  protected given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val shutdownTimeout: FiniteDuration = 1.seconds
  private val idleTimeout: FiniteDuration = 120.seconds

  private val loggerService = Logger.httpRoutes[IO](logHeaders = true, logBody = true)

  protected def buildServer(
      config: ServerConf,
      routes: WebSocketBuilder[IO] => HttpRoutes[IO],
  ): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(config.host)
    .withPort(config.port)
    .withHttpWebSocketApp(ws => loggerService(Router(config.apiPrefix -> routes(ws))).orNotFound)
    .withShutdownTimeout(shutdownTimeout)
    .withIdleTimeout(idleTimeout)
    .build

  protected def buildMaintenance: Resource[IO, Maintenance] =
    for
      service <- MaintenanceService[IO]()
      route <- MaintenanceRoute[IO](service)
    yield Maintenance(service, route)

  protected def buildApp(): Resource[IO, MaintenanceService[IO]]

  def run(args: List[String]): IO[ExitCode] = buildApp()
    .use(_.awaitShutdown).as(ExitCode.Success)

object AppBase:
  final case class Maintenance(service: MaintenanceService[IO], route: MaintenanceRoute[IO])
