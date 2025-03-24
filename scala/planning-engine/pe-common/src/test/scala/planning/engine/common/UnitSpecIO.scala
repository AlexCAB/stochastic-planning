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
| created: 2025-03-22 |||||||||||*/


package planning.engine.common

import cats.MonadThrow
import cats.effect.{IO, Sync}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.{Assertion, Assertions, compatible}
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.matchers.must.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.syntax.all.*

import scala.reflect.ClassTag


abstract class UnitSpecIO extends AsyncWordSpecLike with AsyncIOSpec with Matchers:

  trait Case

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  extension[F[_] : {MonadThrow, Logger}, A](f: F[A])
    def expect(p: A => Boolean)(implicit F: Sync[F]): F[Assertion] = f.asserting(a => assert(p(a)))

    def logValue: F[A] = f.attemptTap:
      case Left(e) => Logger[F].error(e)("F.error:")
      case Right(v) => Logger[F].info(s"F.value($v)").as(v)

  def newCase[C <: Case](test: C => IO[Assertion])(implicit m: ClassTag[C]): IO[Assertion] =
    test(m.runtimeClass.getDeclaredConstructor().newInstance().asInstanceOf[C])