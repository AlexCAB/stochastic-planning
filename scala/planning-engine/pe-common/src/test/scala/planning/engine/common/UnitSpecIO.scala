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

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.matchers.must.Matchers

import scala.reflect.ClassTag

abstract class UnitSpecIO extends AsyncWordSpecLike with AsyncIOSpec with Matchers with SpecLogging:

  trait Case

  def newCase[C <: Case](test: C => IO[Assertion])(implicit m: ClassTag[C]): IO[Assertion] =
    m.runtimeClass.getDeclaredConstructors.toList match
      case c :: Nil if c.getParameterCount == 0 => test(c.newInstance().asInstanceOf[C])
      case c :: Nil if c.getParameterCount == 1 => test(c.newInstance(this).asInstanceOf[C])
      case _ => IO.raiseError(new IllegalArgumentException("Invalid constructor found for test case class"))
