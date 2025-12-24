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
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import planning.engine.api.config.ServerConf
import planning.engine.api.service.maintenance.MaintenanceService

import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class AppBase extends IOApp:
  type Middleware = HttpRoutes[IO] => HttpRoutes[IO]

  protected def buildServer(config: ServerConf, routes: HttpRoutes[IO]): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(config.host)
    .withPort(config.port)
    .withHttpApp(loggerService(Router(config.apiPrefix -> routes)).orNotFound)
    .withShutdownTimeout(shutdownTimeout)
    .build

  protected def buildApp(): Resource[IO, MaintenanceService[IO]]

  protected given LoggerFactory[IO] = Slf4jFactory.create[IO]

  protected val shutdownTimeout: FiniteDuration = 1.seconds
  protected val loggerService: Middleware = Logger.httpRoutes[IO](logHeaders = true, logBody = true)

  def run(args: List[String]): IO[ExitCode] = buildApp()
    .use(_.awaitShutdown).as(ExitCode.Success)
