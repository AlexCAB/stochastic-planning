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
| created: 2025-06-15 |||||||||||*/

package planning.engine.common

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.{Assertion, FutureOutcome}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpecLike

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class UnitSpecWithData extends FixtureAsyncWordSpecLike with AsyncIOSpec with Matchers with SpecLogging:
  trait Case
  type FixtureParam = OneArgAsyncTest

  def newCase[C <: Case](test: (String, C) => IO[Assertion])(implicit
      m: ClassTag[C]
  ): FixtureParam => Future[Assertion] = param =>
    m.runtimeClass.getDeclaredConstructors.toList match
      case c :: Nil if c.getParameterCount == 0 => test(param.name, c.newInstance().asInstanceOf[C])
      case c :: Nil if c.getParameterCount == 1 => test(param.name, c.newInstance(this).asInstanceOf[C])
      case _ => Future.failed(new IllegalArgumentException("Invalid constructor found for test case class"))

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = super.withFixture(test.toNoArgAsyncTest(test))
