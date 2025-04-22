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

package planning.engine.api.service.maintenance

import cats.effect.{Async, Resource}
import cats.effect.std.{Env, CountDownLatch}
import planning.engine.api.model.maintenance.HealthResponse
import planning.engine.api.model.enums.Status
import org.typelevel.log4cats.LoggerFactory

import cats.syntax.all.*

trait MaintenanceServiceLike[F[_]]:
  def getHealth: F[HealthResponse]
  def exit: F[Unit]

class MaintenanceService[F[_]: {Async, Env, LoggerFactory}](latch: CountDownLatch[F]) extends MaintenanceServiceLike[F]:
  private val logger = LoggerFactory[F].getLogger

  def getHealth: F[HealthResponse] =
    for
      status <- Async[F].pure(Status.OK) // TODO: Replace with actual health check logic
      version <- Env[F].get("APP_VERSION").map(_.getOrElse("unknown"))
    yield HealthResponse(status, version)

  def exit: F[Unit] =
    for
      _ <- logger.info("'__exit' endpoint called, application will be terminated.")
      _ <- latch.release
    yield ()

  def awaitShutdown: F[Unit] =
    for
      _ <- latch.await
      _ <- logger.info("Shutting down application...")
    yield ()

object MaintenanceService:
  def apply[F[_]: {Async, Env, LoggerFactory}](): Resource[F, MaintenanceService[F]] =
    Resource.eval(CountDownLatch[F](1)).map(latch => new MaintenanceService[F](latch))
