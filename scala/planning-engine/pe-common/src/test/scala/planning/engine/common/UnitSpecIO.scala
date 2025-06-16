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

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.FutureOutcome
import org.scalatest.wordspec.FixtureAsyncWordSpecLike
import org.scalatest.matchers.must.Matchers

abstract class UnitSpecIO extends FixtureAsyncWordSpecLike with AsyncIOSpec with Matchers with SpecLogging:
  type FixtureParam = String
  override def withFixture(test: OneArgAsyncTest): FutureOutcome = super.withFixture(test.toNoArgAsyncTest(test.name))
