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

import cats.effect.{Async, IO}
import cats.effect.testing.UnsafeRun
import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResource}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpec

abstract class UnitSpecWithResource[R]
    extends FixtureAsyncWordSpec with AsyncIOSpec with CatsResource[IO, R] with Matchers with SpecLogging:

  final override def ResourceAsync = Async[IO]
  final override def ResourceUnsafeRun: UnsafeRun[IO] = UnsafeRun.unsafeRunForCatsIO
