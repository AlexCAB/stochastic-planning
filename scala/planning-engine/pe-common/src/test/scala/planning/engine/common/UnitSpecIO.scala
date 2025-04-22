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
import org.scalatest.{Assertion, compatible}
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.matchers.must.Matchers

import scala.reflect.ClassTag

abstract class UnitSpecIO extends AsyncWordSpecLike with AsyncIOSpec with Matchers with SpecLogging:

  trait Case

  def newCase[C <: Case](test: C => IO[Assertion])(implicit m: ClassTag[C]): IO[Assertion] =
    test(m.runtimeClass.getDeclaredConstructor().newInstance().asInstanceOf[C])
