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
| created: 2025-04-22 |||||||||||*/

package planning.engine.common

import cats.MonadThrow
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource, Sync}
import cats.syntax.all.*
import fansi.Str
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.typelevel.log4cats.slf4j.{Slf4jFactory, Slf4jLogger}
import org.typelevel.log4cats.{Logger, LoggerFactory}

trait SpecLogging:
  self: AsyncIOSpec & Matchers =>

  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  given Logger[IO] = Slf4jLogger.getLoggerFromClass[IO](getClass)

  def logResource[R](resource: Resource[IO, R]): Resource[IO, R] = resource.attempt.flatMap:
    case Right(r) => Resource.eval(Logger[IO].info(s"Acquired resource: $r"))
        .flatMap(_ => Resource.pure(r))

    case Left(err) => Resource.eval(Logger[IO].error(err)("Failed to acquire resource."))
        .flatMap(_ => Resource.raiseError[IO, R, Throwable](err))

  def logIo[T](io: IO[T]): IO[T] = io.attempt.flatMap:
    case Right(value) => Logger[IO].info(s"IO completed successfully: $value").as(value)
    case Left(err)    => Logger[IO].error(err)("IO failed.").flatMap(_ => IO.raiseError(err))

  def logInfo(tn: String, msg: String): IO[Unit] = Logger[IO].info(s"[$tn] $msg")
  def logInfo(tn: String, msg: IO[Str]): IO[Unit] = msg.flatMap(str => Logger[IO].info(s"[$tn] $str"))

  extension [F[_]: {MonadThrow, Logger}, A](f: F[A])
    def expect(p: A => Boolean)(implicit F: Sync[F]): F[Assertion] = f.asserting(a => assert(p(a)))

    def logValue(testName: String, msg: String = "F"): F[A] = f.attemptTap:
      case Left(e)  => Logger[F].info(e)(s"[$testName] $msg, error: ")
      case Right(v) => Logger[F].info(s"[$testName] $msg, value = $v")
