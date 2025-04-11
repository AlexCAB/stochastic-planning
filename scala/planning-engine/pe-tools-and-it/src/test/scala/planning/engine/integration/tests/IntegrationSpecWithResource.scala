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
| created: 2025-03-24 |||||||||||*/

package planning.engine.integration.tests

import cats.effect.testing.UnsafeRun
import cats.effect.{Async, IO, Resource, Sync}
import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResource}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpec
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

abstract class IntegrationSpecWithResource[R]
    extends FixtureAsyncWordSpec with AsyncIOSpec with CatsResource[IO, R] with Matchers:

  final override def ResourceAsync = Async[IO]
  final override def ResourceUnsafeRun: UnsafeRun[IO] = UnsafeRun.unsafeRunForCatsIO

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def logResource(resource: Resource[IO, R]): Resource[IO, R] = resource.attempt.flatMap:
    case Right(r) => Resource.eval(Logger[IO].info(s"Acquired resource: $r"))
        .flatMap(_ => Resource.pure(r))

    case Left(err) => Resource.eval(Logger[IO].error(err)("Failed to acquire resource."))
        .flatMap(_ => Resource.raiseError[IO, R, Throwable](err))

  def logIo[T](io: IO[T]): IO[T] = io.attempt.flatMap:
    case Right(value) => Logger[IO].info(s"IO completed successfully: $value").as(value)
    case Left(err)    => Logger[IO].error(err)("IO failed.").flatMap(_ => IO.raiseError(err))

  override def afterAll(): Unit =
    super.afterAll()
    Thread.sleep(3000) // Wait for all resources to be released
    Logger[IO].info("All tests completed.").unsafeRunSync()
