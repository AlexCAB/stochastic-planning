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
| created: 2026-09-19 |||||||||||*/

package planning.engine.common

import org.mockito.{ArgumentMatchersSugar, IdiomaticStubbing, PostfixVerifications}
import org.scalatest.{Assertion, Succeeded}

/** Idiomatic Mockito syntax (`returns`, `was called`, ...) for ScalaTest, without `AsyncIdiomaticMockito`'s
  * per-test `MockitoSession`.
  *
  * A `MockitoSession` is bound to the thread that started it, but `UnitSpecWithResource` tests are effectively
  * async, so they can finish on another thread. The next test on the original thread then aborts the whole suite with
  * `UnfinishedMockingSessionException`. Use this trait instead of `AsyncIdiomaticMockito` in `UnitSpecWithResource`
  * based specs.
  */
trait MockitoWithResource extends IdiomaticStubbing with ArgumentMatchersSugar with PostfixVerifications:
  self: UnitSpecWithResource[?] =>
  
  override type Verification = Assertion

  override def verification(v: => Any): Verification =
    v
    Succeeded
