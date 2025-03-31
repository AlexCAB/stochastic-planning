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

import cats.effect.{IO, Resource, Sync}
import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResourceIO}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpec

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

abstract class IntegrationSpecWithResource[R]
    extends FixtureAsyncWordSpec with AsyncIOSpec with CatsResourceIO[R] with Matchers:

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def logResource(resource: Resource[IO, R]): Resource[IO, R] = resource.attempt.flatMap:
    case Right(r) => Resource.eval(Logger[IO].info(s"Acquired resource: $r"))
        .flatMap(_ => Resource.pure(r))

    case Left(err) => Resource.eval(Logger[IO].error(err)("Failed to acquire resource."))
        .flatMap(_ => Resource.raiseError[IO, R, Throwable](err))
